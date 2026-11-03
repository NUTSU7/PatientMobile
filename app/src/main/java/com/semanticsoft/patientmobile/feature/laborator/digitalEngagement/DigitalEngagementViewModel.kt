package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.feature.laborator.common.Period
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal const val INITIAL_EXPANDED_ANALYSIS_ID = "tsh"

enum class EngagementLocationFilter(val label: String) {
    ALL("Toate locațiile"),
    BUIUCANI("Buiucani"),
    CENTRU("Centru"),
    RISCANI("Rîșcani"),
    BOTANICA("Botanica")
}

data class EngagementCohortLine(
    val values: List<Float>,
    val observed: Int
)

data class EngagementCohortStat(
    val label: String,
    val valueText: String,
    val changeText: String = ""
)

data class EngagementCohortSnapshot(
    val cohortLabel: String,
    val stats: List<EngagementCohortStat>
)

data class EngagementLocation(
    val location: EngagementLocationFilter,
    val adoptionPercent: Int,
    val firstAccessLabel: String,
    val previousChangePp: Int
)

data class EngagementRepeatedAnalysis(
    val id: String,
    val name: String,
    val method: String,
    val repeatPercent: Int,
    val medianInterval: String,
    val distribution: String,
    val previousChangePp: Int
)

data class DigitalEngagementUiState(
    val selectedPeriod: Period = Period.THIRTY_DAYS,
    val locationFilter: EngagementLocationFilter = EngagementLocationFilter.ALL,
    val compareEnabled: Boolean = true,
    val chartSubtitle: String = "",
    val xLabels: List<String> = emptyList(),
    val cohortLines: List<EngagementCohortLine> = emptyList(),
    val cohortSnapshot: EngagementCohortSnapshot? = null,
    val cohortFootnote: String = "",
    val locationFootnote: String = "",
    val locations: List<EngagementLocation> = emptyList(),
    val repeatedSearchQuery: String = "",
    val repeatedAnalyses: List<EngagementRepeatedAnalysis> = emptyList(),
    val expandedAnalysisId: String? = null
) {
    val activeLocationCount: Int get() = locations.size
    val visibleLocations: List<EngagementLocation>
        get() = when (locationFilter) {
            EngagementLocationFilter.ALL -> locations
            else -> locations.filter { it.location == locationFilter }
        }
    val filteredRepeatedAnalyses: List<EngagementRepeatedAnalysis>
        get() {
            val normalized = repeatedSearchQuery.trim()
            if (normalized.isEmpty()) return repeatedAnalyses
            return repeatedAnalyses.filter { analysis ->
                analysis.name.contains(normalized, ignoreCase = true) ||
                    analysis.method.contains(normalized, ignoreCase = true)
            }
        }
}

@HiltViewModel
class DigitalEngagementViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(buildEngagementMockState(period = Period.THIRTY_DAYS))
    val state: StateFlow<DigitalEngagementUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        // Prototype only: data is mock until laborator analytics endpoints are wired.
    }

    fun onPeriodSelected(period: Period) {
        _state.update { current ->
            val next = buildEngagementMockState(period = period)
            next.copy(
                locationFilter = current.locationFilter,
                compareEnabled = current.compareEnabled,
                expandedAnalysisId = current.expandedAnalysisId,
                repeatedSearchQuery = current.repeatedSearchQuery
            )
        }
    }

    fun onLocationSelected(filter: EngagementLocationFilter) {
        _state.update { it.copy(locationFilter = filter) }
    }

    fun onCompareToggle(enabled: Boolean) {
        _state.update { it.copy(compareEnabled = enabled) }
    }

    fun onRepeatedQueryChange(query: String) {
        _state.update { it.copy(repeatedSearchQuery = query) }
    }

    fun onAnalysisToggle(id: String) {
        _state.update { current ->
            current.copy(
                expandedAnalysisId = if (current.expandedAnalysisId == id) null else id
            )
        }
    }
}
