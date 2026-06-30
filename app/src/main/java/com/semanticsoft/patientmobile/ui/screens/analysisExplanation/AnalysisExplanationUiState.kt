package com.semanticsoft.patientmobile.ui.screens.analysisExplanation

import java.io.File

data class AnalysisExplanationUiState(
    val isLoading: Boolean = true,
    val pdfFile: File? = null,
    val fileName: String = "",
    val explanationText: String = "",
    val errorMessage: String? = null
)
