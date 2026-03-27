package com.semanticsoft.patientmobile.ui.screens.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AnalysisHistoryUiState {
    data object Loading : AnalysisHistoryUiState()
    data object Empty : AnalysisHistoryUiState()
    data class Error(val message: String) : AnalysisHistoryUiState()
    data class Data(
        val documents: List<PatientDocument>,
        val resultsByDocumentId: Map<String, List<MedicalResult>>
    ) : AnalysisHistoryUiState()
}

@HiltViewModel
class AnalysisHistoryViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val medicalResultRepository: MedicalResultRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _stateFlow = MutableStateFlow<AnalysisHistoryUiState>(restoreState())
    val stateFlow: StateFlow<AnalysisHistoryUiState> = _stateFlow.asStateFlow()
    val state: AnalysisHistoryUiState
        get() = _stateFlow.value

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            updateState(AnalysisHistoryUiState.Loading)

            when (val docsResource = documentRepository.getDocuments().first { it !is Resource.Loading }) {
                is Resource.Error -> {
                    updateState(AnalysisHistoryUiState.Error(docsResource.message))
                }

                is Resource.Success -> {
                    val docs = docsResource.data
                    if (docs.isEmpty()) {
                        updateState(AnalysisHistoryUiState.Empty)
                        return@launch
                    }

                    val resultsByDoc = mutableMapOf<String, List<MedicalResult>>()
                    docs.forEach { doc ->
                        val results = when (val resultResource = medicalResultRepository.getByDocumentId(doc.id)
                            .first { it !is Resource.Loading }) {
                            is Resource.Success -> resultResource.data
                            is Resource.Error -> emptyList()
                            Resource.Loading -> emptyList()
                        }
                        resultsByDoc[doc.id] = results
                    }

                    updateState(AnalysisHistoryUiState.Data(docs, resultsByDoc))
                }

                Resource.Loading -> Unit
            }
        }
    }

    private fun restoreState(): AnalysisHistoryUiState {
        return when (savedStateHandle.get<String>(KEY_LAST_STATE_TYPE)) {
            STATE_EMPTY -> AnalysisHistoryUiState.Empty
            STATE_ERROR -> AnalysisHistoryUiState.Error(
                savedStateHandle.get<String>(KEY_LAST_ERROR_MESSAGE) ?: "Eroare la încărcare."
            )
            else -> AnalysisHistoryUiState.Loading
        }
    }

    private fun updateState(newState: AnalysisHistoryUiState) {
        _stateFlow.value = newState
        when (newState) {
            AnalysisHistoryUiState.Loading -> {
                savedStateHandle[KEY_LAST_STATE_TYPE] = STATE_LOADING
            }

            AnalysisHistoryUiState.Empty -> {
                savedStateHandle[KEY_LAST_STATE_TYPE] = STATE_EMPTY
                savedStateHandle.remove<String>(KEY_LAST_ERROR_MESSAGE)
            }

            is AnalysisHistoryUiState.Error -> {
                savedStateHandle[KEY_LAST_STATE_TYPE] = STATE_ERROR
                savedStateHandle[KEY_LAST_ERROR_MESSAGE] = newState.message
            }

            is AnalysisHistoryUiState.Data -> {
                savedStateHandle[KEY_LAST_STATE_TYPE] = STATE_DATA
                savedStateHandle.remove<String>(KEY_LAST_ERROR_MESSAGE)
            }
        }
    }

    companion object {
        private const val KEY_LAST_STATE_TYPE = "analysis_history_last_state_type"
        private const val KEY_LAST_ERROR_MESSAGE = "analysis_history_last_error_message"
        private const val STATE_LOADING = "loading"
        private const val STATE_EMPTY = "empty"
        private const val STATE_ERROR = "error"
        private const val STATE_DATA = "data"
    }
}
