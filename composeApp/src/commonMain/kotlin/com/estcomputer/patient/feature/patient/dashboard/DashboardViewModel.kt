package com.estcomputer.patient.feature.patient.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estcomputer.patient.feature.patient.upload.ProcessingStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val greetingName: String = "",
    val fullName: String = "",
    val role: String = "Pacient",
    val hasUploadedDocuments: Boolean = false,
    val basicIndicators: List<BasicIndicatorItem> = emptyList(),
    val markerSummary: UiMarkerSummary = UiMarkerSummary(0, 0, 0, 0),
    val documentCount: Int = 0,
    val aiSummary: String = "",
    val isAiSummaryLoading: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emptyReason: DashboardEmptyReason? = DashboardEmptyReason.NO_DOCUMENTS
)

enum class DashboardEmptyReason {
    NO_DOCUMENTS,
    PROCESSING,
    NO_RESULTS_EXTRACTED,
    NON_LAB_DOCUMENT
}

sealed class DashboardEvent {
    data object RefreshCompleted : DashboardEvent()
    data class RefreshFailed(val message: String) : DashboardEvent()
    data object LogoutSuccess : DashboardEvent()
    data class LogoutFailure(val message: String) : DashboardEvent()
}

class DashboardViewModel : ViewModel() {

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.LogoutSuccess)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _events.emit(DashboardEvent.RefreshCompleted)
        }
    }

    fun regenerateAiSummary() {
        // Default no-op
    }
}
