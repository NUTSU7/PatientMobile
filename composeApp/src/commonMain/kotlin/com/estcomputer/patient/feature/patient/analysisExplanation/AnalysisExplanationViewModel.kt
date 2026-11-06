package com.estcomputer.patient.feature.patient.analysisExplanation

import java.io.File
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnalysisExplanationViewModel : ViewModel() {

    private val _state = MutableStateFlow(AnalysisExplanationUiState())
    val state: StateFlow<AnalysisExplanationUiState> = _state.asStateFlow()

    fun refresh() {
        // Default no-op
    }
}

data class AnalysisExplanationUiState(
    val isLoading: Boolean = false,
    val pdfFile: File? = null,
    val fileName: String = "",
    val explanationText: String = "",
    val errorMessage: String? = null
)
