package com.semanticsoft.patientmobile.feature.laborator.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class LaboratorPeriod(
    val label: String,
    val activityLabel: String
) {
    SEVEN_DAYS("7 zile", "ULTIMELE 7 ZILE"),
    THIRTY_DAYS("30 zile", "ULTIMELE 30 ZILE"),
    THREE_MONTHS("3 luni", "ULTIMELE 3 LUNI"),
    CUSTOM("Personalizat", "PERIOADĂ PERSONALIZATĂ")
}

data class LaboratorMetric(
    val id: String,
    val label: String,
    val value: String,
    val changeText: String? = null,
    val insufficientData: Boolean = false
)

data class LaboratorActivitySeries(
    val processed: List<Float> = emptyList(),
    val accessed: List<Float> = emptyList(),
    val uniquePatients: List<Float> = emptyList()
)

data class LaboratorTrendSummary(
    val prefix: String,
    val highlight: String,
    val suffix: String,
    val footnote: String
)

data class LaboratorTopAnalysis(
    val rank: Int,
    val title: String,
    val subtitle: String,
    val percent: Int? = null,
    val insufficientData: Boolean = false
)

data class LaboratorDashboardUiState(
    val labName: String = "Invitro Chișinău",
    val lastSyncLabel: String = "Ultima sincronizare: —",
    val selectedPeriod: LaboratorPeriod = LaboratorPeriod.THIRTY_DAYS,
    val compareEnabled: Boolean = true,
    val metrics: List<LaboratorMetric> = emptyList(),
    val activity: LaboratorActivitySeries = LaboratorActivitySeries(),
    val trend: LaboratorTrendSummary = LaboratorTrendSummary(
        prefix = "",
        highlight = "",
        suffix = "",
        footnote = ""
    ),
    val topAnalyses: List<LaboratorTopAnalysis> = emptyList()
)

@HiltViewModel
class LaboratorDashboardViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(
        buildLaboratorDashboardMockState(
            period = LaboratorPeriod.THIRTY_DAYS,
            compareEnabled = true,
            lastSyncLabel = formatLastSyncLabel()
        )
    )
    val state: StateFlow<LaboratorDashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(lastSyncLabel = formatLastSyncLabel()) }
    }

    fun onPeriodSelected(period: LaboratorPeriod) {
        _state.update { current ->
            buildLaboratorDashboardMockState(
                period = period,
                compareEnabled = current.compareEnabled,
                lastSyncLabel = current.lastSyncLabel
            )
        }
    }

    fun onCompareToggle(enabled: Boolean) {
        _state.update { it.copy(compareEnabled = enabled) }
    }

    private fun formatLastSyncLabel(): String {
        val formatted = LocalDateTime.now().format(SYNC_FORMATTER)
        return "Ultima sincronizare: $formatted"
    }

    private companion object {
        val SYNC_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
    }
}
