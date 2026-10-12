package com.semanticsoft.patientmobile.feature.laborator.dpa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DpaUiState(
    val accepted: Boolean = false
)

sealed interface DpaEvent {
    data object ContinueToPortal : DpaEvent
}

@HiltViewModel
class DpaViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DpaUiState())
    val state: StateFlow<DpaUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DpaEvent>()
    val events: SharedFlow<DpaEvent> = _events.asSharedFlow()

    fun onAcceptedChange(value: Boolean) {
        _state.update { it.copy(accepted = value) }
    }

    fun onContinueClick() {
        if (!_state.value.accepted) return
        viewModelScope.launch {
            _events.emit(DpaEvent.ContinueToPortal)
        }
    }
}
