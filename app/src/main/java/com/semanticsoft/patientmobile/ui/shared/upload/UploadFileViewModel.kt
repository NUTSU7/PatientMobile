package com.semanticsoft.patientmobile.ui.shared.upload

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.di.IoDispatcher
import com.semanticsoft.patientmobile.domain.model.ProcessingStatus
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.ExtractionTarget
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.OcrRepository
import com.semanticsoft.patientmobile.domain.repository.ProcessingCoordinator
import com.semanticsoft.patientmobile.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
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
    private val documentRepository: DocumentRepository,
    private val globalSyncManager: GlobalSyncManager,
    private val ocrRepository: OcrRepository,
    private val processingCoordinator: ProcessingCoordinator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(UploadFileUiState())
    val state: StateFlow<UploadFileUiState> = _state.asStateFlow()

    val processingStatus: StateFlow<ProcessingStatus>
        get() = processingCoordinator.status

    private val _events = MutableSharedFlow<UploadFileEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    fun resumeProcessingIfPending() {
        processingCoordinator.resumeIfPending()
    }

    fun onFilesSelected(uris: List<String>) {
        val oversizeErrors = mutableListOf<ErrorFileEntry>()
        val validFiles = uris.mapNotNull { uri ->
            val file = resolveFile(uri) ?: return@mapNotNull null
            if (!file.exists()) return@mapNotNull null
            val extension = file.extension.lowercase()
            if (extension !in ALLOWED_EXTENSIONS) return@mapNotNull null
            if (file.length() > MAX_UPLOAD_BYTES) {
                oversizeErrors.add(ErrorFileEntry(file.name, "Fi\u0219ier prea mare (max 10 MB)."))
                return@mapNotNull null
            }
            val normalized = if (ImageNormalizer.shouldNormalize(file)) {
                ImageNormalizer.normalize(file)
            } else null
            val uploadFile = normalized ?: file
            val displayName = if (normalized != null) {
                val dot = file.name.lastIndexOf('.')
                val base = if (dot > 0) file.name.substring(0, dot) else file.name
                "${base}-normalizata.jpg"
            } else file.name
            val displaySize = uploadFile.length()
            SelectedFile(
                name = displayName,
                uri = uploadFile.absolutePath.let { "file://$it" },
                sizeBytes = displaySize,
                mimeType = if (normalized != null) "image/jpeg" else resolveMimeType(file)
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
                isSystemicError = false,
                errorFiles = oversizeErrors
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
                if (f.status == UploadStatus.PENDING || f.status == UploadStatus.PENDING_FORCE) idx else null
            }

            val checksumMap = mutableMapOf<Int, String>()
            for (index in pendingIndices) {
                val f = _state.value.selectedFiles[index]
                val file = resolveFile(f.uri) ?: continue
                val checksum = withContext(ioDispatcher) { computeSha256(file) }
                if (checksum != null) {
                    checksumMap[index] = checksum
                }
            }

            val duplicateIdByChecksum = mutableMapOf<String, String>()
            if (checksumMap.isNotEmpty()) {
                val uniqueChecksums = checksumMap.values.toList()
                when (val dupResult = documentRepository.checkDuplicates(uniqueChecksums)) {
                    is ApiResult.Success -> {
                        dupResult.data.forEach { duplicateIdByChecksum[it.checksum] = it.existingDocumentId }
                        val matchedChecksums = dupResult.data.map { it.checksum }.toSet()
                        if (matchedChecksums.isNotEmpty()) {
                            _state.update { current ->
                                val files = current.selectedFiles.toMutableList()
                                for ((index, checksum) in checksumMap) {
                                    if (checksum in matchedChecksums && index in files.indices) {
                                        val f = files[index]
                                        if (f.status == UploadStatus.PENDING) {
                                            files[index] = f.copy(
                                                status = UploadStatus.PENDING_FORCE,
                                                errorMessage = "Fi\u0219ier deja \u00EEnc\u0103rcat."
                                            )
                                        }
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
                    it.status == UploadStatus.PENDING || it.status == UploadStatus.PENDING_FORCE
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

                val forceUpload = _state.value.selectedFiles[pendingIndex].status == UploadStatus.PENDING_FORCE
                when (val uploadResult = documentRepository.uploadDocument(f, force = forceUpload)) {
                    is ApiResult.Success -> {
                        val documentId = uploadResult.data.id
                        Log.d(TAG, "Triggering startExtraction for documentId=$documentId")
                        when (val extractionResult = startExtractionWithRetry(documentId)) {
                            is ApiResult.Success -> {
                                val runId = extractionResult.data
                                Log.d(TAG, "startExtraction success: runId=$runId documentId=$documentId")
                                _state.update { current ->
                                    val afterSuccess = current.selectedFiles.toMutableList()
                                    afterSuccess[pendingIndex] = current.selectedFiles[pendingIndex].copy(
                                        status = UploadStatus.SUCCESS,
                                        documentId = documentId,
                                        errorMessage = null
                                    )
                                    current.copy(
                                        selectedFiles = afterSuccess,
                                        extractionJobs = current.extractionJobs + ExtractionJob(documentId, runId)
                                    )
                                }
                                _events.emit(UploadFileEvent.ExtractionStarted(documentId, runId))
                            }
                            else -> {
                                Log.e(TAG, "startExtraction failed after retries for documentId=$documentId")
                                _state.update { current ->
                                    val afterError = current.selectedFiles.toMutableList()
                                    afterError[pendingIndex] = current.selectedFiles[pendingIndex].copy(
                                        status = UploadStatus.ERROR,
                                        documentId = documentId,
                                        errorMessage = EXTRACTION_FAILED_MESSAGE,
                                        errorType = ErrorType.NETWORK,
                                        errorIcon = FileErrorIcon.RETRY
                                    )
                                    current.copy(selectedFiles = afterError)
                                }
                            }
                        }
                    }
                    is ApiResult.HttpError -> {
                        if (uploadResult.code == 409) {
                            val existingId = checksumMap[pendingIndex]?.let { duplicateIdByChecksum[it] }
                            _state.update { current ->
                                val afterSuccess = current.selectedFiles.toMutableList()
                                afterSuccess[pendingIndex] = current.selectedFiles[pendingIndex].copy(
                                    status = UploadStatus.SUCCESS,
                                    documentId = existingId,
                                    errorMessage = null
                                )
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

            val allDone = _state.value.selectedFiles.none {
                it.status == UploadStatus.PENDING || it.status == UploadStatus.PENDING_FORCE || it.status == UploadStatus.UPLOADING
            }
            val hasErrors = _state.value.selectedFiles.any { it.status == UploadStatus.ERROR }
            val hasNetworkError = _state.value.selectedFiles.any { it.errorType == ErrorType.NETWORK }
            val anySuccess = _state.value.selectedFiles.any { it.status == UploadStatus.SUCCESS }
            val clearFiles = allDone && !hasErrors

            _state.update {
                it.copy(
                    isUploading = false,
                    uploadComplete = clearFiles,
                    hasNetworkError = hasNetworkError,
                    selectedFiles = if (clearFiles) emptyList() else it.selectedFiles
                )
            }

            if (anySuccess) {
                val extractionJobs = _state.value.extractionJobs
                if (extractionJobs.isNotEmpty()) {
                    processingCoordinator.start(
                        extractionJobs.map { ExtractionTarget(it.documentId, it.runId) }
                    )
                }
                globalSyncManager.triggerSync()
                _events.emit(UploadFileEvent.AllFilesUploaded)
            }
        }
    }

    fun retryFile(index: Int) {
        if (_state.value.isUploading) return
        val files = _state.value.selectedFiles.toMutableList()
        if (index !in files.indices) return
        val current = files[index]
        if (current.documentId != null && current.errorMessage == EXTRACTION_FAILED_MESSAGE) {
            retryExtraction(index)
            return
        }
        files[index] = current.copy(
            status = if (current.errorMessage?.contains("deja") == true) UploadStatus.PENDING_FORCE else UploadStatus.PENDING,
            errorMessage = null, errorType = null, errorIcon = null
        )
        _state.update { it.copy(selectedFiles = files) }
        startUpload()
    }

    private fun retryExtraction(index: Int) {
        viewModelScope.launch {
            val documentId = _state.value.selectedFiles.getOrNull(index)?.documentId ?: return@launch
            _state.update { current ->
                val updated = current.selectedFiles.toMutableList()
                updated[index] = current.selectedFiles[index].copy(
                    status = UploadStatus.UPLOADING,
                    errorMessage = null,
                    errorType = null,
                    errorIcon = null
                )
                current.copy(selectedFiles = updated, isUploading = true)
            }
            when (val extractionResult = startExtractionWithRetry(documentId)) {
                is ApiResult.Success -> {
                    val runId = extractionResult.data
                    _state.update { current ->
                        val updated = current.selectedFiles.toMutableList()
                        updated[index] = current.selectedFiles[index].copy(status = UploadStatus.SUCCESS)
                        current.copy(
                            selectedFiles = updated,
                            isUploading = false,
                            extractionJobs = current.extractionJobs + ExtractionJob(documentId, runId)
                        )
                    }
                    processingCoordinator.start(listOf(ExtractionTarget(documentId, runId)))
                    globalSyncManager.triggerSync()
                    _events.emit(UploadFileEvent.AllFilesUploaded)
                }
                else -> {
                    _state.update { current ->
                        val updated = current.selectedFiles.toMutableList()
                        updated[index] = current.selectedFiles[index].copy(
                            status = UploadStatus.ERROR,
                            errorMessage = EXTRACTION_FAILED_MESSAGE,
                            errorType = ErrorType.NETWORK,
                            errorIcon = FileErrorIcon.RETRY
                        )
                        current.copy(selectedFiles = updated, isUploading = false)
                    }
                }
            }
        }
    }

    private suspend fun startExtractionWithRetry(documentId: String): ApiResult<String> {
        var attempt = 0
        var result: ApiResult<String> = ApiResult.NetworkError
        while (attempt < EXTRACTION_MAX_ATTEMPTS) {
            result = ocrRepository.startExtraction(documentId)
            if (result is ApiResult.Success) return result
            attempt++
            if (attempt < EXTRACTION_MAX_ATTEMPTS) delay(EXTRACTION_RETRY_DELAY_MS)
        }
        return result
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
        private const val TAG = "UploadFileVM"
        private val ALLOWED_EXTENSIONS = setOf("pdf", "jpg", "jpeg", "png")
        private const val MAX_UPLOAD_BYTES = 10L * 1024L * 1024L
        private const val EXTRACTION_MAX_ATTEMPTS = 3
        private const val EXTRACTION_RETRY_DELAY_MS = 2_000L
        internal const val EXTRACTION_FAILED_MESSAGE = "Extragerea nu a pornit."

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