package com.semanticsoft.patientmobile.ui.screens.uploadFile

/**
 * UI state for the Upload File modal screen
 * Tracks file selection and upload progress
 */
data class UploadFileUiState(
    val selectedFileName: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null
)

/**
 * Upload source types for file selection
 */
enum class UploadSource {
    FILE_PICKER,    // "Încarcă fisier" - local file picker
    GOOGLE_DRIVE,   // "Google Drive" integration
    DROPBOX         // "Dropbox" integration
}
