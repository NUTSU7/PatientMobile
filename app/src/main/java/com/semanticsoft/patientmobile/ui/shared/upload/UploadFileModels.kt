package com.semanticsoft.patientmobile.ui.shared.upload

enum class UploadStatus { PENDING, PENDING_FORCE, UPLOADING, SUCCESS, ERROR }

enum class ErrorType { DUPLICATE, FILE_ERROR, NETWORK, SYSTEMIC }

data class ProcessingPollState(
    val isPolling: Boolean = false,
    val totalJobs: Int = 0,
    val completedJobs: Int = 0,
    val failedJobs: Int = 0,
    val message: String = "",
    val isSlowWarning: Boolean = false,
    val isTimedOut: Boolean = false
) {
    val pendingJobs: Int get() = totalJobs - completedJobs - failedJobs
    val isDone: Boolean get() = totalJobs > 0 && pendingJobs <= 0
    val isActive: Boolean get() = isPolling && !isDone && !isTimedOut
}

data class ExtractionJobResult(
    val documentId: String,
    val runId: String,
    val status: String,
    val resultCount: Int = 0
)

data class ErrorFileEntry(
    val name: String,
    val message: String
)

enum class FileErrorIcon { RETRY, DELETE }

data class SelectedFile(
    val name: String,
    val uri: String,
    val sizeBytes: Long,
    val mimeType: String,
    val status: UploadStatus = UploadStatus.PENDING,
    val errorMessage: String? = null,
    val errorType: ErrorType? = null,
    val errorIcon: FileErrorIcon? = null
)

data class ExtractionJob(
    val documentId: String,
    val runId: String
)

data class UploadFileUiState(
    val selectedFiles: List<SelectedFile> = emptyList(),
    val isUploading: Boolean = false,
    val uploadComplete: Boolean = false,
    val isSystemicError: Boolean = false,
    val hasNetworkError: Boolean = false,
    val extractionJobs: List<ExtractionJob> = emptyList(),
    val processingPollState: ProcessingPollState = ProcessingPollState(),
    val errorFiles: List<ErrorFileEntry> = emptyList()
)

sealed class UploadFileEvent {
    data object AllFilesUploaded : UploadFileEvent()
    data class ExtractionStarted(val documentId: String, val runId: String) : UploadFileEvent()
    data class ProcessingComplete(
        val totalJobs: Int,
        val successCount: Int,
        val failedCount: Int,
        val timedOut: Boolean
    ) : UploadFileEvent()
}
