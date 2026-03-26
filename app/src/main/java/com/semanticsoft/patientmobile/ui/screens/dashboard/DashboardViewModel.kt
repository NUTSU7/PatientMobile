package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.data.model.BasicIndicatorItem
import com.semanticsoft.patientmobile.data.model.GeneralMarkerCardItem
import com.semanticsoft.patientmobile.data.model.MarkerCategoryItem
import com.semanticsoft.patientmobile.data.model.MarkerSummary
import com.semanticsoft.patientmobile.data.model.WarningCardItem
import com.semanticsoft.patientmobile.data.repository.MockPatientRepository
import com.semanticsoft.patientmobile.data.repository.PatientRepository

data class DashboardUiState(
    val greetingName: String = "",
    val lastAnalysisDate: String = "15 Mar 2026",
    val attentionItems: List<AttentionItem> = emptyList(),
    val basicIndicators: List<BasicIndicatorItem> = emptyList(),
    val markerCategories: List<MarkerCategoryItem> = emptyList(),
    val generalMarkerCards: List<GeneralMarkerCardItem> = emptyList(),
    val markerSummary: MarkerSummary = MarkerSummary(0, 0, 0, 0),
    val aiSummary: String = "",
    val warningCards: List<WarningCardItem> = emptyList()
)

class DashboardViewModel(
    private val repository: PatientRepository = MockPatientRepository()
) : ViewModel() {
    private val demoAttentionItems = listOf(
        AttentionItem("TSH", "0.3", "mIU/L", "Atenție"),
        AttentionItem("Vitamina D", "18", "ng/mL", "Atenție"),
        AttentionItem("Colesterol LDL", "4.5", "mmol/L", "Atenție")
    )


    val state: DashboardUiState = DashboardUiState(
        greetingName = repository.getDashboardGreetingName(),
        attentionItems = repository.getAttentionItems().ifEmpty { demoAttentionItems },
        basicIndicators = repository.getBasicIndicators(),
        markerCategories = repository.getMarkerCategories(),
        generalMarkerCards = repository.getGeneralMarkerCards(),
        markerSummary = repository.getMarkerSummary(),
        aiSummary = repository.getAiSummary(),
        warningCards = repository.getWarningCards()
    )
}
