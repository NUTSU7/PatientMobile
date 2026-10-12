package com.semanticsoft.patientmobile.core.ui.shared.upload

enum class UploadStatus { PENDING, PENDING_FORCE, UPLOADING, SUCCESS, ERROR }

enum class ErrorType { DUPLICATE, FILE_ERROR, NETWORK, SYSTEMIC }

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
    val errorIcon: FileErrorIcon? = null,
    val documentId: String? = null
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
