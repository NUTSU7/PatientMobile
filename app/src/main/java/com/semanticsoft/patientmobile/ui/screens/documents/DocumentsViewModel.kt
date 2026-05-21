package com.semanticsoft.patientmobile.ui.screens.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentUiItem(
    val id: String,
    val originalFileName: String,
    val fileType: String,
    val observedAt: String?,
    val uploadedAt: String
)

sealed interface DocumentListState {
    data object Loading : DocumentListState
    data class Success(val documents: List<DocumentUiItem>) : DocumentListState
    data class Error(val message: String) : DocumentListState
}

sealed interface DocumentUploadState {
    data object Idle : DocumentUploadState
    data object Uploading : DocumentUploadState
    data class Success(val document: PatientDocument) : DocumentUploadState
    data class Error(val message: String) : DocumentUploadState
}

sealed interface DocumentEvent {
    data class OpenFile(val file: File) : DocumentEvent
}

data class DocumentFilters(
    val search: String = "",
    val dateFrom: String = "",
    val dateTo: String = ""
)

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<DocumentListState>(DocumentListState.Loading)
    val listState: StateFlow<DocumentListState> = _listState.asStateFlow()

    private val _uploadState = MutableStateFlow<DocumentUploadState>(DocumentUploadState.Idle)
    val uploadState: StateFlow<DocumentUploadState> = _uploadState.asStateFlow()

    private val _events = MutableSharedFlow<DocumentEvent>()
    val events: SharedFlow<DocumentEvent> = _events.asSharedFlow()

    private val _filters = MutableStateFlow(DocumentFilters())
    val filters: StateFlow<DocumentFilters> = _filters.asStateFlow()

    init {
        loadDocuments()
    }

    fun onSearchChange(value: String) {
        _filters.update { it.copy(search = value) }
    }

    fun onDateFromChange(value: String) {
        _filters.update { it.copy(dateFrom = value) }
    }

    fun onDateToChange(value: String) {
        _filters.update { it.copy(dateTo = value) }
    }

    fun applyFilters() {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _listState.update { DocumentListState.Loading }

            val f = _filters.value
            when (val result = documentRepository.getDocuments(
                page = 0,
                size = 50,
                search = f.search.takeIf { it.isNotBlank() },
                dateFrom = f.dateFrom.takeIf { it.isNotBlank() },
                dateTo = f.dateTo.takeIf { it.isNotBlank() }
            )) {
                is ApiResult.Success -> {
                    _listState.update {
                        DocumentListState.Success(result.data.map { it.toUiItem() })
                    }
                }
                else -> {
                    _listState.update {
                        DocumentListState.Error(result.toUserMessage())
                    }
                }
            }
        }
    }

    fun uploadDocument(file: File) {
        viewModelScope.launch {
            _uploadState.update { DocumentUploadState.Uploading }

            when (val result = documentRepository.uploadDocument(file)) {
                is ApiResult.Success -> {
                    _uploadState.update { DocumentUploadState.Success(result.data) }
                    loadDocuments()
                }
                is ApiResult.HttpError -> {
                    val message = when (result.code) {
                        413 -> "Fi\u0219ierul este prea mare (maxim 10 MB)."
                        415 -> "Format neacceptat (se accept\u0103 doar PDF, JPG, PNG)."
                        else -> result.toUserMessage()
                    }
                    _uploadState.update { DocumentUploadState.Error(message) }
                }
                else -> {
                    _uploadState.update { DocumentUploadState.Error(result.toUserMessage()) }
                }
            }
        }
    }

    fun resetUploadState() {
        _uploadState.update { DocumentUploadState.Idle }
    }

    fun downloadAndOpen(documentId: String) {
        viewModelScope.launch {
            when (val result = documentRepository.downloadDocument(documentId)) {
                is ApiResult.Success -> {
                    _events.emit(DocumentEvent.OpenFile(result.data))
                }
                else -> { }
            }
        }
    }

    fun bulkDelete(documentIds: List<String>) {
        viewModelScope.launch {
            when (documentRepository.bulkDelete(documentIds)) {
                is ApiResult.Success -> loadDocuments()
                else -> { }
            }
        }
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            when (documentRepository.deleteDocument(documentId)) {
                is ApiResult.Success -> loadDocuments()
                else -> { }
            }
        }
    }

    private fun PatientDocument.toUiItem(): DocumentUiItem = DocumentUiItem(
        id = id,
        originalFileName = originalFileName,
        fileType = mimeType,
        observedAt = observedAt?.toString(),
        uploadedAt = uploadedAt.toString()
    )
}
