package com.semanticsoft.patientmobile.ui.shared.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class UploadFileViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UploadFileUiState())
    val state: StateFlow<UploadFileUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UploadFileEvent>()
    val events = _events.asSharedFlow()

    fun onFilesSelected(uris: List<String>) {
        val validFiles = uris.mapNotNull { uri ->
            val file = resolveFile(uri) ?: return@mapNotNull null
            if (!file.exists()) return@mapNotNull null
            val extension = file.extension.lowercase()
            if (extension !in ALLOWED_EXTENSIONS) return@mapNotNull null
            if (file.length() > MAX_UPLOAD_BYTES) return@mapNotNull null
            SelectedFile(
                name = file.name,
                uri = uri,
                sizeBytes = file.length(),
                mimeType = resolveMimeType(file)
            )
        }

        if (validFiles.isEmpty()) {
            val current = _state.value.selectedFiles
            if (current.all { it.status != UploadStatus.ERROR }) return
            val updated = current.map { file ->
                if (file.status == UploadStatus.PENDING || file.status == UploadStatus.UPLOADING) {
                    file.copy(errorMessage = "Niciun fi\u0219ier valid selectat.")
                } else file
            }
            _state.update { it.copy(selectedFiles = updated) }
            return
        }

        _state.update {
            it.copy(
                selectedFiles = it.selectedFiles + validFiles,
                uploadComplete = false,
                isSystemicError = false
            )
        }
    }

    fun startUpload() {
        if (_state.value.isUploading) return
        if (_state.value.selectedFiles.none {
                it.status == UploadStatus.PENDING || it.status == UploadStatus.ERROR
            }) return

        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, isSystemicError = false) }

            val pendingIndices = _state.value.selectedFiles.mapIndexedNotNull { idx, f ->
                if (f.status == UploadStatus.PENDING) idx else null
            }

            val checksumMap = mutableMapOf<Int, String>()
            for (index in pendingIndices) {
                val f = _state.value.selectedFiles[index]
                val file = resolveFile(f.uri) ?: continue
                val checksum = withContext(Dispatchers.IO) { computeSha256(file) }
                if (checksum != null) {
                    checksumMap[index] = checksum
                }
            }

            if (checksumMap.isNotEmpty()) {
                val uniqueChecksums = checksumMap.values.toList()
                when (val dupResult = documentRepository.checkDuplicates(uniqueChecksums)) {
                    is ApiResult.Success -> {
                        val matchedChecksums = dupResult.data.map { it.checksum }.toSet()
                        if (matchedChecksums.isNotEmpty()) {
                            _state.update { current ->
                                val files = current.selectedFiles.toMutableList()
                                for ((index, checksum) in checksumMap) {
                                    if (checksum in matchedChecksums) {
                                        files[index] = files[index].copy(
                                            status = UploadStatus.ERROR,
                                            errorMessage = "Fi\u0219ierul este deja \u00EEnc\u0103rcat.",
                                            errorType = ErrorType.DUPLICATE,
                                            errorIcon = FileErrorIcon.DELETE
                                        )
                                    }
                                }
                                current.copy(selectedFiles = files)
                            }
                        }
                    }
                    else -> {}
                }
            }

            while (true) {
                val pendingIndex = _state.value.selectedFiles.indexOfFirst {
                    it.status == UploadStatus.PENDING
                }
                if (pendingIndex == -1) break

                val file = _state.value.selectedFiles[pendingIndex]

                _state.update { current ->
                    val updated = current.selectedFiles.toMutableList()
                    updated[pendingIndex] = file.copy(status = UploadStatus.UPLOADING, errorMessage = null)
                    current.copy(selectedFiles = updated)
                }

                val f = resolveFile(file.uri)
                if (f == null) {
                    _state.update { current ->
                        val afterError = current.selectedFiles.toMutableList()
                        afterError[pendingIndex] = file.copy(
                            status = UploadStatus.ERROR,
                            errorMessage = "Fi\u0219ier neg\u0103sit.",
                            errorType = ErrorType.FILE_ERROR,
                            errorIcon = FileErrorIcon.DELETE
                        )
                        current.copy(selectedFiles = afterError)
                    }
                    continue
                }

                when (val uploadResult = documentRepository.uploadDocument(f)) {
                    is ApiResult.Success -> {
                        _state.update { current ->
                            val afterSuccess = current.selectedFiles.toMutableList()
                            afterSuccess.removeAt(pendingIndex)
                            current.copy(selectedFiles = afterSuccess)
                        }
                    }
                    is ApiResult.HttpError -> {
                        if (uploadResult.code == 409) {
                            _state.update { current ->
                                val afterSuccess = current.selectedFiles.toMutableList()
                                afterSuccess.removeAt(pendingIndex)
                                current.copy(selectedFiles = afterSuccess)
                            }
                            continue
                        }
                        val message = when (uploadResult.code) {
                            413 -> "Fi\u0219ier prea mare (max 10 MB)."
                            415 -> "Format nesuportat (PDF, JPG, JPEG, PNG)."
                            422 -> "Fi\u0219ierul a e\u0219uat scanarea de securitate."
                            503 -> "Serverul este \u00EEn mentenan\u021B\u0103."
                            else -> uploadResult.message.ifBlank { "Eroare de conexiune." }
                        }
                        val errorType = when (uploadResult.code) {
                            413, 415, 422 -> ErrorType.FILE_ERROR
                            503 -> ErrorType.SYSTEMIC
                            else -> ErrorType.NETWORK
                        }
                        val keepIcon = when (errorType) {
                            ErrorType.NETWORK -> FileErrorIcon.RETRY
                            else -> FileErrorIcon.DELETE
                        }
                        _state.update { current ->
                            val afterError = current.selectedFiles.toMutableList()
                            afterError[pendingIndex] = file.copy(
                                status = UploadStatus.ERROR,
                                errorMessage = message,
                                errorType = errorType,
                                errorIcon = keepIcon
                            )
                            current.copy(
                                selectedFiles = afterError,
                                isSystemicError = errorType == ErrorType.SYSTEMIC
                            )
                        }
                    }
                    is ApiResult.NetworkError -> {
                        _state.update { current ->
                            val afterError = current.selectedFiles.toMutableList()
                            afterError[pendingIndex] = file.copy(
                                status = UploadStatus.ERROR,
                                errorMessage = "Nu exist\u0103 conexiune la internet.",
                                errorType = ErrorType.SYSTEMIC,
                                errorIcon = FileErrorIcon.RETRY
                            )
                            current.copy(
                                selectedFiles = afterError,
                                isSystemicError = true
                            )
                        }
                    }
                    is ApiResult.AuthError -> {
                        _state.update { current ->
                            val afterError = current.selectedFiles.toMutableList()
                            afterError[pendingIndex] = file.copy(
                                status = UploadStatus.ERROR,
                                errorMessage = "Session expired.",
                                errorType = ErrorType.NETWORK,
                                errorIcon = FileErrorIcon.RETRY
                            )
                            current.copy(selectedFiles = afterError)
                        }
                    }
                }
            }

            val allGone = _state.value.selectedFiles.isEmpty()
            val hasErrors = _state.value.selectedFiles.any { it.status == UploadStatus.ERROR }
            val hasNetworkError = _state.value.selectedFiles.any { it.errorType == ErrorType.NETWORK }

            _state.update {
                it.copy(
                    isUploading = false,
                    uploadComplete = allGone && !hasErrors,
                    hasNetworkError = hasNetworkError
                )
            }

            if (allGone && !hasErrors) {
                _events.emit(UploadFileEvent.AllFilesUploaded)
            }
        }
    }

    fun retryFile(index: Int) {
        if (_state.value.isUploading) return
        val files = _state.value.selectedFiles.toMutableList()
        if (index !in files.indices) return
        files[index] = files[index].copy(status = UploadStatus.PENDING, errorMessage = null, errorType = null, errorIcon = null)
        _state.update { it.copy(selectedFiles = files) }
        startUpload()
    }

    fun removeFile(index: Int) {
        if (_state.value.isUploading) return
        val updated = _state.value.selectedFiles.toMutableList().apply { removeAt(index) }
        val hasSystemic = updated.any { it.errorType == ErrorType.SYSTEMIC }
        val hasNetwork = updated.any { it.errorType == ErrorType.NETWORK }
        _state.update { it.copy(selectedFiles = updated, isSystemicError = hasSystemic, hasNetworkError = hasNetwork) }
    }

    fun resetUploadComplete() {
        _state.update { it.copy(uploadComplete = false) }
    }

    fun reset() {
        _state.update { UploadFileUiState() }
    }

    private fun resolveFile(uri: String): File? {
        val path = when {
            uri.startsWith("file://") -> uri.removePrefix("file://")
            uri.startsWith("/") -> uri
            else -> return null
        }
        return File(path)
    }

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("pdf", "jpg", "jpeg", "png")
        private const val MAX_UPLOAD_BYTES = 10L * 1024L * 1024L

        private fun resolveMimeType(file: File): String = when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }

        @JvmStatic
        private fun computeSha256(file: File): String? {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                file.inputStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            } catch (_: Exception) {
                null
            }
        }
    }
}