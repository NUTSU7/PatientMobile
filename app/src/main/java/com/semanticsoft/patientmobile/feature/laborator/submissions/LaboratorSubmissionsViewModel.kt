package com.semanticsoft.patientmobile.feature.laborator.submissions

import androidx.lifecycle.ViewModel
import com.semanticsoft.patientmobile.domain.model.laborator.LabSubmission
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LaboratorSubmissionsUiState(
    val isLoading: Boolean = false,
    val submissions: List<LabSubmission> = emptyList()
)

@HiltViewModel
class LaboratorSubmissionsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LaboratorSubmissionsUiState())
    val state: StateFlow<LaboratorSubmissionsUiState> = _state.asStateFlow()
}
