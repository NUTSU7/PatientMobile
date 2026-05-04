package com.semanticsoft.patientmobile.ui.shared.upload

enum class UploadStatus { PENDING, UPLOADING, SUCCESS, ERROR }

enum class ErrorType { DUPLICATE, FILE_ERROR, NETWORK, SYSTEMIC }

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

data class UploadFileUiState(
    val selectedFiles: List<SelectedFile> = emptyList(),
    val isUploading: Boolean = false,
    val uploadComplete: Boolean = false,
    val isSystemicError: Boolean = false,
    val hasNetworkError: Boolean = false
)

sealed class UploadFileEvent {
    data object AllFilesUploaded : UploadFileEvent()
}
