package com.estcomputer.patient.feature.patient.reportResults

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class ReportResultsUiState(
    val isLoading: Boolean = false,
    val reportTitle: String = "",
    val observedAtLabel: String = "",
    val clinicalType: String? = null,
    val summary: String? = null,
    val requiresReview: Boolean = false,
    val groupedResults: List<ResultGroup> = emptyList(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val isNonLabReport: Boolean = false
)

class ReportResultsViewModel : ViewModel() {

    private val _state = MutableStateFlow(ReportResultsUiState())
    val state: StateFlow<ReportResultsUiState> = _state.asStateFlow()

    fun loadResults() {
        // Default no-op
    }
}
