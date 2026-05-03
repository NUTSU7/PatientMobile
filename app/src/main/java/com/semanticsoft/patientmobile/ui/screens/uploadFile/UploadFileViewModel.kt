package com.semanticsoft.patientmobile.ui.screens.uploadFile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.exceptions.DocumentScanningUnavailableException
import com.semanticsoft.patientmobile.util.exceptions.FileTooLargeException
import com.semanticsoft.patientmobile.util.exceptions.MalwareDetectedException
import com.semanticsoft.patientmobile.util.exceptions.UnsupportedMediaTypeException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the Upload File modal screen
 * Manages file selection, upload progress, and error handling
 */
@HiltViewModel
class UploadFileViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(UploadFileUiState())
        private set

    init {
        state = state.copy(
            selectedFileName = savedStateHandle[KEY_SELECTED_FILE_NAME],
            uploadProgress = savedStateHandle[KEY_UPLOAD_PROGRESS] ?: 0f,
            errorMessage = savedStateHandle[KEY_ERROR_MESSAGE]
        )
    }

    /**
     * Handle file selection from drag-and-drop or file picker
     */
    fun onFileSelected(fileName: String) {
        state = state.copy(
            selectedFileName = fileName,
            errorMessage = null
        )
        savedStateHandle[KEY_SELECTED_FILE_NAME] = fileName
        savedStateHandle[KEY_ERROR_MESSAGE] = null
    }

    /**
     * Clear selected file
     */
    fun clearSelection() {
        state = state.copy(selectedFileName = null)
        savedStateHandle[KEY_SELECTED_FILE_NAME] = null
    }

    /**
     * Handle upload initiation from specified source
     */
    fun uploadFile(source: UploadSource) {
        if (state.isUploading) return

        when (source) {
            UploadSource.GOOGLE_DRIVE -> {
                setError("Google Drive nu este încă integrat pentru upload direct.")
                return
            }

            UploadSource.DROPBOX -> {
                setError("Dropbox nu este încă integrat pentru upload direct.")
                return
            }

            UploadSource.FILE_PICKER -> Unit
        }

        val selected = state.selectedFileName
        if (selected.isNullOrBlank()) {
            setError("Selectează un fișier înainte de upload.")
            return
        }

        val file = File(selected)
        if (!file.exists()) {
            setError("Selectează un fișier local valid pentru a verifica tipul și dimensiunea.")
            return
        }

        val extension = file.extension.lowercase()
        if (extension !in allowedExtensions) {
            setError("Format nesuportat (pdf, jpg, jpeg, png).")
            return
        }

        if (file.length() > maxUploadBytes) {
            setError("Fișier prea mare (max 10 MB).")
            return
        }

        viewModelScope.launch {
            state = state.copy(isUploading = true, uploadProgress = 0.05f, errorMessage = null)
            savedStateHandle[KEY_UPLOAD_PROGRESS] = 0.05f

            try {
                delay(120)
                state = state.copy(uploadProgress = 0.35f)
                savedStateHandle[KEY_UPLOAD_PROGRESS] = 0.35f

                documentRepository.uploadDocument(file)

                delay(120)
                state = state.copy(uploadProgress = 1f)
                savedStateHandle[KEY_UPLOAD_PROGRESS] = 1f

                state = state.copy(
                    isUploading = false,
                    selectedFileName = null,
                    errorMessage = null
                )
                savedStateHandle[KEY_SELECTED_FILE_NAME] = null
                savedStateHandle[KEY_ERROR_MESSAGE] = null
            } catch (throwable: Throwable) {
                val message = when (throwable) {
                    is FileTooLargeException -> "Fișier prea mare (max 10 MB)."
                    is UnsupportedMediaTypeException -> "Format nesuportat (pdf, jpg, jpeg, png)."
                    is MalwareDetectedException -> "Fișierul a eșuat scanarea de securitate."
                    is DocumentScanningUnavailableException -> "Server unavailable, try again later."
                    else -> throwable.message ?: "Upload eșuat. Încearcă din nou."
                }
                setError(message)
            }
        }
    }

    /**
     * Reset upload state
     */
    fun resetUpload() {
        state = state.copy(
            isUploading = false,
            uploadProgress = 0f,
            errorMessage = null
        )
        savedStateHandle[KEY_UPLOAD_PROGRESS] = 0f
        savedStateHandle[KEY_ERROR_MESSAGE] = null
    }

    /**
     * Handle upload error
     */
    fun setError(message: String) {
        state = state.copy(
            isUploading = false,
            uploadProgress = 0f,
            errorMessage = message
        )
        savedStateHandle[KEY_UPLOAD_PROGRESS] = 0f
        savedStateHandle[KEY_ERROR_MESSAGE] = message
    }

    companion object {
        private val allowedExtensions = setOf("pdf", "jpg", "jpeg", "png")
        private const val maxUploadBytes = 10L * 1024L * 1024L

        private const val KEY_SELECTED_FILE_NAME = "upload_selected_file_name"
        private const val KEY_UPLOAD_PROGRESS = "upload_progress"
        private const val KEY_ERROR_MESSAGE = "upload_error_message"
    }
}
