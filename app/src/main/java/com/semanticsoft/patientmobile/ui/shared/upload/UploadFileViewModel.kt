package com.semanticsoft.patientmobile.ui.shared.upload

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.di.IoDispatcher
import com.semanticsoft.patientmobile.data.remote.api.ApiConstants
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.OcrRepository
import com.semanticsoft.patientmobile.domain.repository.UploadStateManager
import com.semanticsoft.patientmobile.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
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
    private val dashboardRepository: com.semanticsoft.patientmobile.domain.repository.DashboardRepository,
    private val uploadStateManager: UploadStateManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(UploadFileUiState())
    val state: StateFlow<UploadFileUiState> = _state.asStateFlow()

    val processingPollState: StateFlow<ProcessingPollState>
        get() = _processingPollState
    private val _processingPollState = MutableStateFlow(ProcessingPollState())

    private val _events = MutableSharedFlow<UploadFileEvent>()
    val events = _events.asSharedFlow()

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

            if (checksumMap.isNotEmpty()) {
                val uniqueChecksums = checksumMap.values.toList()
                when (val dupResult = documentRepository.checkDuplicates(uniqueChecksums)) {
                    is ApiResult.Success -> {
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
                        _state.update { current ->
                            val afterSuccess = current.selectedFiles.toMutableList()
                            afterSuccess.removeAt(pendingIndex)
                            current.copy(selectedFiles = afterSuccess)
                        }
                        withContext(ioDispatcher) {
                            Log.d(TAG, "Triggering startExtraction for documentId=$documentId")
                            when (val extractionResult = ocrRepository.startExtraction(documentId)) {
                                is ApiResult.Success -> {
                                    val runId = extractionResult.data
                                    Log.d(TAG, "startExtraction success: runId=$runId documentId=$documentId")
                                    _state.update { current ->
                                        current.copy(
                                            extractionJobs = current.extractionJobs + ExtractionJob(documentId, runId)
                                        )
                                    }
                                    _events.emit(UploadFileEvent.ExtractionStarted(documentId, runId))
                                }
                                is ApiResult.HttpError -> {
                                    Log.e(TAG, "startExtraction HTTP ${extractionResult.code}: ${extractionResult.message} for documentId=$documentId")
                                }
                                is ApiResult.NetworkError -> {
                                    Log.e(TAG, "startExtraction NetworkError for documentId=$documentId")
                                }
                                is ApiResult.AuthError -> {
                                    Log.e(TAG, "startExtraction AuthError for documentId=$documentId")
                                }
                            }
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
                val extractionJobs = _state.value.extractionJobs
                if (extractionJobs.isNotEmpty()) {
                    startPollingExtractions()
                } else {
                    globalSyncManager.triggerSync()
                    _events.emit(UploadFileEvent.AllFilesUploaded)
                }
            }
        }
    }

    fun retryFile(index: Int) {
        if (_state.value.isUploading) return
        val files = _state.value.selectedFiles.toMutableList()
        if (index !in files.indices) return
        val current = files[index]
        files[index] = current.copy(
            status = if (current.errorMessage?.contains("deja") == true) UploadStatus.PENDING_FORCE else UploadStatus.PENDING,
            errorMessage = null, errorType = null, errorIcon = null
        )
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
        _processingPollState.update { ProcessingPollState() }
    }

    private fun startPollingExtractions() {
        val jobs = _state.value.extractionJobs
        if (jobs.isEmpty()) {
            _processingPollState.update { ProcessingPollState() }
            return
        }
        val pollState = ProcessingPollState(
            isPolling = true,
            totalJobs = jobs.size,
            message = "Fi\u0219ierele s-au \u00EEnc\u0103rcat, iar acum se proceseaz\u0103..."
        )
        _processingPollState.update { pollState }
        viewModelScope.launch {
            uploadStateManager.setProcessing(true)
            try {
                globalSyncManager.triggerSync()
                val startTime = System.currentTimeMillis()
                val results = mutableMapOf<String, ExtractionJobResult>()
                coroutineScope {
                    jobs.map { job ->
                        async {
                            val extracted = pollExtractionJob(job)
                            synchronized(results) { results[job.documentId] = extracted }
                            val completed = results.values.count { it.status == "SUCCESS" }
                            val failed = results.values.count { it.status == "FAILED" }
                            val elapsed = System.currentTimeMillis() - startTime
                            val message = when {
                                elapsed >= ApiConstants.OCR_POLL_MAX_RETRIES * ApiConstants.OCR_POLL_DELAY_MS -> {
                                    _processingPollState.update { it.copy(isTimedOut = true) }
                                    "Procesarea a durat prea mult. Po\u021Bi reveni mai t\u00E2rziu."
                                }
                                elapsed >= ApiConstants.OCR_POLL_SLOW_WARNING_MS -> {
                                    _processingPollState.update { it.copy(isSlowWarning = true) }
                                    "Procesarea dureaz\u0103 mai mult dec\u00E2t de obicei..."
                                }
                                else -> pollState.message
                            }
                            _processingPollState.update {
                                it.copy(
                                    completedJobs = completed,
                                    failedJobs = failed,
                                    message = "$message (${completed + failed} din ${pollState.totalJobs})"
                                )
                            }
                            globalSyncManager.triggerSync()
                        }
                    }.awaitAll()
                }
                val successCount = results.values.count { it.status == "SUCCESS" }
                val failedCount = results.values.count { it.status == "FAILED" }
                val timedOut = _processingPollState.value.isTimedOut
                _processingPollState.update { ProcessingPollState() }
                globalSyncManager.triggerSync()
                launch {
                    try { dashboardRepository.regenerateAiSummary() } catch (_: Exception) {}
                }
                delay(2_500L)
                globalSyncManager.triggerSync()
                _events.emit(UploadFileEvent.ProcessingComplete(
                    totalJobs = pollState.totalJobs,
                    successCount = successCount,
                    failedCount = failedCount,
                    timedOut = timedOut
                ))
            } finally {
                uploadStateManager.setProcessing(false)
            }
        }
    }

    private suspend fun pollExtractionJob(job: ExtractionJob): ExtractionJobResult {
        var retries = 0
        while (retries < ApiConstants.OCR_POLL_MAX_RETRIES) {
            when (val result = ocrRepository.getExtractionStatus(job.documentId, job.runId)) {
                is ApiResult.Success -> {
                    val extraction = result.data
                    if (extraction.status == OcrStatus.SUCCESS || extraction.status == OcrStatus.COMPLETED) {
                        return ExtractionJobResult(job.documentId, job.runId, "SUCCESS", extraction.reports.size)
                    }
                    if (extraction.status == OcrStatus.FAILED) {
                        return ExtractionJobResult(job.documentId, job.runId, "FAILED")
                    }
                }
                else -> {}
            }
            retries++
            delay(ApiConstants.OCR_POLL_DELAY_MS)
        }
        return ExtractionJobResult(job.documentId, job.runId, "TIMEOUT")
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