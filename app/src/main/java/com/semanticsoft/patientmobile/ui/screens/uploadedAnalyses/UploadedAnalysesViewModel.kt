package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
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

data class UploadedAnalysesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allDocuments: List<PatientDocument> = emptyList()
) {
    val documents: List<PatientDocument>
        get() = allDocuments

    val latestUploadDate: String?
        get() = documents
            .maxByOrNull { it.uploadedAt }
            ?.uploadedAt
            ?.toString()
            ?.substringBefore("T")
}

@HiltViewModel
class UploadedAnalysesViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UploadedAnalysesUiState(isLoading = true))
    val state: StateFlow<UploadedAnalysesUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    init {
        refreshDocuments()
    }

    fun refreshDocuments() {
        viewModelScope.launch {
            val hasData = _state.value.allDocuments.isNotEmpty()
            if (!hasData) {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
            }

            when (val result = documentRepository.getDocuments(0, 20)) {
                is ApiResult.HttpError -> {
                    if (hasData) {
                        _refreshErrors.emit(result.toUserMessage())
                        _state.update { it.copy(isLoading = false) }
                    } else {
                        _state.update { it.copy(isLoading = false, errorMessage = result.toUserMessage()) }
                    }
                }
                is ApiResult.NetworkError -> {
                    if (hasData) {
                        _refreshErrors.emit(result.toUserMessage())
                        _state.update { it.copy(isLoading = false) }
                    } else {
                        _state.update { it.copy(isLoading = false, errorMessage = result.toUserMessage()) }
                    }
                }
                is ApiResult.AuthError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.toUserMessage()) }
                }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            allDocuments = result.data
                        )
                    }
                }
            }
        }
    }
}
