package com.semanticsoft.patientmobile.ui.shared.upload

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.exceptions.DocumentScanningUnavailableException
import com.semanticsoft.patientmobile.util.exceptions.DuplicateDocumentException
import com.semanticsoft.patientmobile.util.exceptions.FileTooLargeException
import com.semanticsoft.patientmobile.util.exceptions.MalwareDetectedException
import com.semanticsoft.patientmobile.util.exceptions.NoInternetException
import com.semanticsoft.patientmobile.util.exceptions.UnsupportedMediaTypeException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UploadFileViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(UploadFileUiState())
        private set

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
            val current = state.selectedFiles
            if (current.all { it.status != UploadStatus.ERROR }) return
            val updated = current.map { file ->
                if (file.status == UploadStatus.PENDING || file.status == UploadStatus.UPLOADING) {
                    file.copy(errorMessage = "Niciun fi\u0219ier valid selectat.")
                } else file
            }
            state = state.copy(selectedFiles = updated)
            return
        }

        state = state.copy(
            selectedFiles = state.selectedFiles + validFiles,
            uploadComplete = false,
            isSystemicError = false
        )
    }

    fun startUpload() {
        if (state.isUploading) return
        if (state.selectedFiles.none {
                it.status == UploadStatus.PENDING || it.status == UploadStatus.ERROR
            }) return

        viewModelScope.launch {
            state = state.copy(isUploading = true, isSystemicError = false)

            val pendingIndices = state.selectedFiles.mapIndexedNotNull { idx, f ->
                if (f.status == UploadStatus.PENDING) idx else null
            }

            val checksumMap = mutableMapOf<Int, String>()
            for (index in pendingIndices) {
                val f = state.selectedFiles[index]
                val file = resolveFile(f.uri) ?: continue
                val checksum = computeSha256(file)
                if (checksum != null) {
                    checksumMap[index] = checksum
                }
            }

            if (checksumMap.isNotEmpty()) {
                val uniqueChecksums = checksumMap.values.toList()
                val matches = documentRepository.checkDuplicates(uniqueChecksums)
                val matchedChecksums = matches.map { it.checksum }.toSet()

                if (matchedChecksums.isNotEmpty()) {
                    val files = state.selectedFiles.toMutableList()
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
                    state = state.copy(selectedFiles = files)
                }
            }

            while (true) {
                val pendingIndex = state.selectedFiles.indexOfFirst {
                    it.status == UploadStatus.PENDING
                }
                if (pendingIndex == -1) break

                val file = state.selectedFiles[pendingIndex]

                val updated = state.selectedFiles.toMutableList()
                updated[pendingIndex] = file.copy(status = UploadStatus.UPLOADING, errorMessage = null)
                state = state.copy(selectedFiles = updated)

                try {
                    val f = resolveFile(file.uri) ?: throw IllegalStateException("Fi\u0219ier neg\u0103sit.")
                    documentRepository.uploadDocument(f)

                    val afterSuccess = state.selectedFiles.toMutableList()
                    afterSuccess.removeAt(pendingIndex)
                    state = state.copy(selectedFiles = afterSuccess)
                } catch (throwable: Throwable) {
                    if (throwable is DuplicateDocumentException) {
                        val afterSuccess = state.selectedFiles.toMutableList()
                        afterSuccess.removeAt(pendingIndex)
                        state = state.copy(selectedFiles = afterSuccess)
                        continue
                    }
                    val message = when (throwable) {
                        is FileTooLargeException -> "Fi\u0219ier prea mare (max 10 MB)."
                        is UnsupportedMediaTypeException -> "Format nesuportat (PDF, JPG, JPEG, PNG)."
                        is MalwareDetectedException -> "Fi\u0219ierul a e\u0219uat scanarea de securitate."
                        is DocumentScanningUnavailableException -> "Serverul este \u00EEn mentenan\u021B\u0103."
                        is NoInternetException -> throwable.message ?: "Nu exist\u0103 conexiune la internet."
                        else -> "Eroare de conexiune. Apas\u0103 pentru a re\u00EEncerca."
                    }
                    val errorType = when (throwable) {
                        is FileTooLargeException, is UnsupportedMediaTypeException,
                        is MalwareDetectedException -> ErrorType.FILE_ERROR
                        is DocumentScanningUnavailableException, is NoInternetException -> ErrorType.SYSTEMIC
                        else -> ErrorType.NETWORK
                    }
                    val keepIcon = when (errorType) {
                        ErrorType.NETWORK -> FileErrorIcon.RETRY
                        else -> FileErrorIcon.DELETE
                    }
                    val afterError = state.selectedFiles.toMutableList()
                    afterError[pendingIndex] = file.copy(
                        status = UploadStatus.ERROR,
                        errorMessage = message,
                        errorType = errorType,
                        errorIcon = keepIcon
                    )
                    state = state.copy(
                        selectedFiles = afterError,
                        isSystemicError = errorType == ErrorType.SYSTEMIC
                    )
                }
            }

            val allGone = state.selectedFiles.isEmpty()
            val hasErrors = state.selectedFiles.any { it.status == UploadStatus.ERROR }
            val hasNetworkError = state.selectedFiles.any { it.errorType == ErrorType.NETWORK }

            state = state.copy(
                isUploading = false,
                uploadComplete = allGone && !hasErrors,
                hasNetworkError = hasNetworkError
            )

            if (allGone && !hasErrors) {
                _events.emit(UploadFileEvent.AllFilesUploaded)
            }
        }
    }

    fun retryFile(index: Int) {
        if (state.isUploading) return
        val files = state.selectedFiles.toMutableList()
        if (index !in files.indices) return
        files[index] = files[index].copy(status = UploadStatus.PENDING, errorMessage = null, errorType = null, errorIcon = null)
        state = state.copy(selectedFiles = files)
        startUpload()
    }

    fun removeFile(index: Int) {
        if (state.isUploading) return
        val updated = state.selectedFiles.toMutableList().apply { removeAt(index) }
        val hasSystemic = updated.any { it.errorType == ErrorType.SYSTEMIC }
        val hasNetwork = updated.any { it.errorType == ErrorType.NETWORK }
        state = state.copy(
            selectedFiles = updated,
            isSystemicError = hasSystemic,
            hasNetworkError = hasNetwork
        )
    }

    fun resetUploadComplete() {
        state = state.copy(uploadComplete = false)
    }

    fun reset() {
        state = UploadFileUiState()
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
