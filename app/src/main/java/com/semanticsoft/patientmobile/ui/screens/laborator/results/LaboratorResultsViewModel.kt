package com.semanticsoft.patientmobile.ui.screens.laborator.results

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.domain.model.laborator.LabResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LaboratorResultsUiState(
    val isLoading: Boolean = false,
    val results: List<LabResult> = emptyList()
)

@HiltViewModel
class LaboratorResultsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LaboratorResultsUiState())
    val state: StateFlow<LaboratorResultsUiState> = _state.asStateFlow()
}
