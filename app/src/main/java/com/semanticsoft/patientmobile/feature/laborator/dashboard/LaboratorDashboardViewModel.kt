package com.semanticsoft.patientmobile.feature.laborator.dashboard

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.domain.model.laborator.LabSubmission
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LaboratorDashboardUiState(
    val isLoading: Boolean = false,
    val submissions: List<LabSubmission> = emptyList()
)

@HiltViewModel
class LaboratorDashboardViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LaboratorDashboardUiState())
    val state: StateFlow<LaboratorDashboardUiState> = _state.asStateFlow()
}
