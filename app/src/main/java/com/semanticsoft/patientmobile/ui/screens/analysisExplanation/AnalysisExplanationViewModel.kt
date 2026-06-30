package com.semanticsoft.patientmobile.ui.screens.analysisExplanation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AnalysisExplanationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
    private val globalSyncManager: GlobalSyncManager
) : ViewModel() {

    private val documentId: String = savedStateHandle["documentId"] ?: ""

    private val _state = MutableStateFlow(AnalysisExplanationUiState())
    val state: StateFlow<AnalysisExplanationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            globalSyncManager.syncEvents.collect { refresh() }
        }
    }

    fun refresh() {
        if (documentId.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val fileDeferred = async { documentRepository.downloadDocumentFile(documentId) }
            val explanationDeferred = async { documentRepository.getDocumentExplanation(documentId) }

            val fileResult = fileDeferred.await()
            val explanationResult = explanationDeferred.await()

            val pdfFile = when (fileResult) {
                is ApiResult.Success -> fileResult.data
                else -> null
            }

            val fileName = pdfFile?.name ?: ""

            val explanationText = when (explanationResult) {
                is ApiResult.Success -> explanationResult.data
                else -> ""
            }

            val errorMessage = when (fileResult) {
                is ApiResult.Success -> null
                else -> fileResult.toUserMessage()
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    pdfFile = pdfFile,
                    fileName = fileName,
                    explanationText = explanationText,
                    errorMessage = errorMessage
                )
            }
        }
    }
}
