package com.semanticsoft.patientmobile.feature.laborator.results

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.domain.model.laborator.LabResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ResultsUiState(
    val isLoading: Boolean = false,
    val results: List<LabResult> = emptyList()
)

@HiltViewModel
class ResultsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ResultsUiState())
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()
}
