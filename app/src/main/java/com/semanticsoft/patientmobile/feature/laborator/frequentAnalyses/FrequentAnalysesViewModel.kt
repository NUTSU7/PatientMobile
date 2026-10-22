package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal const val FREQUENT_ANALYSES_PAGE_COUNT = 3
internal const val FREQUENT_ANALYSES_PAGE_SIZE = 8

enum class FrequentAnalysesSort {
    RESULTS,
    UNIQUE_PATIENTS
}

data class FrequentAnalysis(
    val id: String,
    val name: String,
    val subtitle: String,
    val accessedResults: Int,
    val uniquePatients: Int?
)

data class FrequentAnalysesUiState(
    val selectedPeriod: LaboratorPeriod = LaboratorPeriod.THIRTY_DAYS,
    val query: String = "",
    val sort: FrequentAnalysesSort = FrequentAnalysesSort.RESULTS,
    val currentPage: Int = 1,
    val analyses: List<FrequentAnalysis> = emptyList(),
    val totalCount: Int = 0
)

@HiltViewModel
class FrequentAnalysesViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(
        buildState(
            selectedPeriod = LaboratorPeriod.THIRTY_DAYS,
            query = "",
            sort = FrequentAnalysesSort.RESULTS,
            currentPage = 1
        )
    )
    val state: StateFlow<FrequentAnalysesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { current ->
            buildState(
                selectedPeriod = current.selectedPeriod,
                query = current.query,
                sort = current.sort,
                currentPage = current.currentPage
            )
        }
    }

    fun onPeriodSelected(period: LaboratorPeriod) {
        _state.update { current ->
            buildState(
                selectedPeriod = period,
                query = current.query,
                sort = current.sort,
                currentPage = current.currentPage
            )
        }
    }

    fun onQueryChange(query: String) {
        _state.update { current ->
            buildState(
                selectedPeriod = current.selectedPeriod,
                query = query,
                sort = current.sort,
                currentPage = 1
            )
        }
    }

    fun onSortSelected(sort: FrequentAnalysesSort) {
        _state.update { current ->
            buildState(
                selectedPeriod = current.selectedPeriod,
                query = current.query,
                sort = sort,
                currentPage = 1
            )
        }
    }

    fun onPageSelected(page: Int) {
        _state.update { current ->
            current.copy(currentPage = page.coerceIn(1, FREQUENT_ANALYSES_PAGE_COUNT))
        }
    }

    fun onPreviousPage() {
        onPageSelected(_state.value.currentPage - 1)
    }

    fun onNextPage() {
        onPageSelected(_state.value.currentPage + 1)
    }

    private fun buildState(
        selectedPeriod: LaboratorPeriod,
        query: String,
        sort: FrequentAnalysesSort,
        currentPage: Int
    ): FrequentAnalysesUiState {
        val filtered = filterAnalyses(mockFrequentAnalyses(), query)
        val sorted = sortAnalyses(filtered, sort)
        val totalCount = if (query.isBlank()) {
            FREQUENT_ANALYSES_TOTAL_COUNT
        } else {
            sorted.size
        }

        return FrequentAnalysesUiState(
            selectedPeriod = selectedPeriod,
            query = query,
            sort = sort,
            currentPage = currentPage.coerceIn(1, FREQUENT_ANALYSES_PAGE_COUNT),
            analyses = sorted,
            totalCount = totalCount
        )
    }

    private fun filterAnalyses(
        analyses: List<FrequentAnalysis>,
        query: String
    ): List<FrequentAnalysis> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return analyses

        return analyses.filter { analysis ->
            analysis.name.contains(normalized, ignoreCase = true) ||
                analysis.subtitle.contains(normalized, ignoreCase = true)
        }
    }

    private fun sortAnalyses(
        analyses: List<FrequentAnalysis>,
        sort: FrequentAnalysesSort
    ): List<FrequentAnalysis> {
        return when (sort) {
            FrequentAnalysesSort.RESULTS -> analyses.sortedWith(
                compareByDescending<FrequentAnalysis> { it.accessedResults }
                    .thenByDescending { it.uniquePatients ?: -1 }
                    .thenBy { it.name }
            )

            FrequentAnalysesSort.UNIQUE_PATIENTS -> analyses.sortedWith(
                compareByDescending<FrequentAnalysis> { it.uniquePatients ?: -1 }
                    .thenByDescending { it.accessedResults }
                    .thenBy { it.name }
            )
        }
    }
}
