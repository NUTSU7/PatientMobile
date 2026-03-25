package com.semanticsoft.patientmobile.ui.screens.upload

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class UploadUiState(
    val documentName: String = "",
    val notes: String = ""
)

class UploadViewModel : ViewModel() {
    var state by mutableStateOf(UploadUiState())
        private set

    fun onDocumentNameChange(value: String) {
        state = state.copy(documentName = value)
    }

    fun onNotesChange(value: String) {
        state = state.copy(notes = value)
    }

    fun upload() {
        // API integration will be added in the next step.
    }
}
