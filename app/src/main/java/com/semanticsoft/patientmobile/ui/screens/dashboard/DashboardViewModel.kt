package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.data.model.MarkerSummary
import com.semanticsoft.patientmobile.data.repository.MockPatientRepository
import com.semanticsoft.patientmobile.data.repository.PatientRepository

data class DashboardUiState(
    val greetingName: String = "",
    val lastAnalysisDate: String = "15 Mar 2026",
    val attentionItems: List<AttentionItem> = emptyList(),
    val markerSummary: MarkerSummary = MarkerSummary(0, 0, 0, 0),
    val aiSummary: String = ""
)

class DashboardViewModel(
    private val repository: PatientRepository = MockPatientRepository()
) : ViewModel() {
    val state: DashboardUiState = DashboardUiState(
        greetingName = repository.getDashboardGreetingName(),
        attentionItems = repository.getAttentionItems(),
        markerSummary = repository.getMarkerSummary(),
        aiSummary = repository.getAiSummary()
    )
}
