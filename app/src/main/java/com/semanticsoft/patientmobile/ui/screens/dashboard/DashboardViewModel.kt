package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.data.model.BasicIndicatorItem
import com.semanticsoft.patientmobile.data.model.IndicatorSegments
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.data.model.IndicatorTrendDirection
import com.semanticsoft.patientmobile.data.model.MarkerSummary
import com.semanticsoft.patientmobile.data.repository.MockPatientRepository
import com.semanticsoft.patientmobile.data.repository.PatientRepository

data class DashboardUiState(
    val greetingName: String = "",
    val lastAnalysisDate: String = "15 Mar 2026",
    val attentionItems: List<AttentionItem> = emptyList(),
    val basicIndicators: List<BasicIndicatorItem> = emptyList(),
    val markerSummary: MarkerSummary = MarkerSummary(0, 0, 0, 0),
    val aiSummary: String = ""
)

class DashboardViewModel(
    private val repository: PatientRepository = MockPatientRepository()
) : ViewModel() {
    private val demoAttentionItems = listOf(
        AttentionItem("TSH", "0.3", "mIU/L", "Atenție"),
        AttentionItem("Vitamina D", "18", "ng/mL", "Atenție"),
        AttentionItem("Colesterol LDL", "4.5", "mmol/L", "Atenție")
    )

    private val demoBasicIndicators = listOf(
        BasicIndicatorItem(
            title = "Hemoglobina",
            value = "14.2",
            unit = "g/dL",
            status = IndicatorStatus.NORMAL,
            trendDirection = IndicatorTrendDirection.STABLE,
            trendDelta = "",
            trendDescription = "Stabil față de ultima analiză",
            markerPosition = 0.34f,
            segments = IndicatorSegments(0.60f, 0.20f, 0.20f)
        ),
        BasicIndicatorItem(
            title = "Glucoza a jeun",
            value = "6.8",
            unit = "mmol/L",
            status = IndicatorStatus.BORDERLINE,
            trendDirection = IndicatorTrendDirection.UP,
            trendDelta = "0.6",
            trendDescription = "față de ultima analiză",
            markerPosition = 0.66f,
            segments = IndicatorSegments(0.60f, 0.20f, 0.20f)
        )
    )

    val state: DashboardUiState = DashboardUiState(
        greetingName = repository.getDashboardGreetingName(),
        attentionItems = repository.getAttentionItems().ifEmpty { demoAttentionItems },
        basicIndicators = repository.getBasicIndicators().ifEmpty { demoBasicIndicators },
        markerSummary = repository.getMarkerSummary(),
        aiSummary = repository.getAiSummary()
    )
}
