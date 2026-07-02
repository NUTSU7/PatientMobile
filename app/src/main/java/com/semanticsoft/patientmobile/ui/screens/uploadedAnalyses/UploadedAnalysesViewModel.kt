package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components.SortCriteria
import com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components.SortOrder
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

sealed class DialogState {
    data object None : DialogState()
    data class Rename(val documentId: String, val error: String? = null) : DialogState()
    data class SingleDelete(val documentId: String) : DialogState()
    data class BulkDelete(val documentIds: Set<String>) : DialogState()
}

sealed class UploadedAnalysesEffect {
    data class OpenFile(val file: File) : UploadedAnalysesEffect()
}

data class UploadedAnalysesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allDocuments: List<PatientDocument> = emptyList(),
    val sortCriteria: SortCriteria = SortCriteria.DATA_INCARCARII,
    val sortOrder: SortOrder = SortOrder.DESCRESCATOR,
    val isSelectionMode: Boolean = false,
    val selectedDocumentIds: Set<String> = emptySet(),
    val dialogState: DialogState = DialogState.None,
    val isDeleting: Boolean = false
) {
    val documents: List<PatientDocument>
        get() = allDocuments.sortedWith(sortComparator)

    val showRenameDialog: Boolean
        get() = dialogState is DialogState.Rename

    val showDeleteDialog: Boolean
        get() = dialogState is DialogState.SingleDelete

    val showBulkDeleteDialog: Boolean
        get() = dialogState is DialogState.BulkDelete

    val latestUploadDate: String?
        get() = allDocuments
            .maxByOrNull { it.uploadedAt }
            ?.uploadedAt
            ?.toString()
            ?.substringBefore("T")

    private val sortComparator: Comparator<PatientDocument>
        get() {
            val base = when (sortCriteria) {
                SortCriteria.DENUMIRE -> compareBy<PatientDocument> { it.originalFileName.lowercase() }
                SortCriteria.DATA_INCARCARII -> compareBy<PatientDocument> { it.uploadedAt }
                SortCriteria.TIP -> compareBy<PatientDocument> { it.mimeType }
                SortCriteria.DIMENSIUNE -> compareBy<PatientDocument> { it.fileSizeBytes }
            }
            return if (sortOrder == SortOrder.CRESCATOR) base else base.reversed()
        }
}

@HiltViewModel
class UploadedAnalysesViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val globalSyncManager: GlobalSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(UploadedAnalysesUiState(isLoading = true))
    val state: StateFlow<UploadedAnalysesUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    private val _effects = MutableSharedFlow<UploadedAnalysesEffect>()
    val effects: SharedFlow<UploadedAnalysesEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            globalSyncManager.syncEvents.collect { refreshDocuments() }
        }
    }

    fun onSortCriteriaSelected(criteria: SortCriteria) {
        _state.update { it.copy(sortCriteria = criteria) }
    }

    fun onSortOrderSelected(order: SortOrder) {
        _state.update { it.copy(sortOrder = order) }
    }

    // ── Rename Dialog ─────────────────────────────────────────────────────

    fun onShowRenameDialog(documentId: String) {
        _state.update { it.copy(dialogState = DialogState.Rename(documentId)) }
    }

    fun onDismissRenameDialog() {
        _state.update { it.copy(dialogState = DialogState.None) }
    }

    fun onRenameDocument(documentId: String, newName: String) {
        viewModelScope.launch {
            val finalName = if (newName.endsWith(".pdf", ignoreCase = true)) newName else "$newName.pdf"
            when (val result = documentRepository.renameDocument(documentId, finalName)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(dialogState = DialogState.None) }
                    globalSyncManager.triggerSync()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _state.update {
                        it.copy(dialogState = DialogState.Rename(documentId, error = result.toUserMessage()))
                    }
                }
            }
        }
    }

    // ── Single Delete Dialog ──────────────────────────────────────────────

    fun onShowDeleteDialog(documentId: String) {
        _state.update { it.copy(dialogState = DialogState.SingleDelete(documentId)) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(dialogState = DialogState.None) }
    }

    fun onDeleteDocument(documentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            when (val result = documentRepository.deleteDocument(documentId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            dialogState = DialogState.None,
                            isDeleting = false,
                            allDocuments = it.allDocuments.filterNot { doc -> doc.id == documentId }
                        )
                    }
                    globalSyncManager.triggerSync()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _refreshErrors.emit(result.toUserMessage())
                    _state.update { it.copy(dialogState = DialogState.None, isDeleting = false) }
                }
            }
        }
    }

    // ── Selection Mode ────────────────────────────────────────────────────

    fun enterSelectionMode(documentId: String) {
        _state.update { it.copy(isSelectionMode = true, selectedDocumentIds = setOf(documentId)) }
    }

    fun toggleDocumentSelection(documentId: String) {
        _state.update { current ->
            if (current.selectedDocumentIds.contains(documentId)) {
                val newSelection = current.selectedDocumentIds - documentId
                if (newSelection.isEmpty()) {
                    current.copy(isSelectionMode = false, selectedDocumentIds = emptySet())
                } else {
                    current.copy(selectedDocumentIds = newSelection)
                }
            } else {
                current.copy(selectedDocumentIds = current.selectedDocumentIds + documentId)
            }
        }
    }

    fun exitSelectionMode() {
        _state.update {
            it.copy(
                isSelectionMode = false,
                selectedDocumentIds = emptySet(),
                dialogState = DialogState.None
            )
        }
    }

    // ── Bulk Delete Dialog ────────────────────────────────────────────────

    fun onShowBulkDeleteDialog(ids: Set<String>) {
        if (ids.isEmpty()) return
        _state.update { it.copy(dialogState = DialogState.BulkDelete(ids)) }
    }

    fun onDismissBulkDeleteDialog() {
        _state.update { it.copy(dialogState = DialogState.None) }
    }

    fun onBulkDelete() {
        viewModelScope.launch {
            val bulkState = _state.value.dialogState as? DialogState.BulkDelete ?: return@launch
            val ids = bulkState.documentIds.toList()
            val deletedIds = ids.toSet()
            _state.update { it.copy(isDeleting = true) }
            when (val result = documentRepository.bulkDelete(ids)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isSelectionMode = false,
                            selectedDocumentIds = emptySet(),
                            dialogState = DialogState.None,
                            isDeleting = false,
                            allDocuments = it.allDocuments.filterNot { doc -> doc.id in deletedIds }
                        )
                    }
                    globalSyncManager.triggerSync()
                }
                is ApiResult.HttpError,
                is ApiResult.NetworkError,
                is ApiResult.AuthError -> {
                    _state.update {
                        it.copy(isDeleting = false, dialogState = DialogState.None)
                    }
                    _refreshErrors.emit(result.toUserMessage())
                }
            }
        }
    }

    // ── Download Document ─────────────────────────────────────────────────

    fun onDownloadDocument(documentId: String) {
        viewModelScope.launch {
            when (val result = documentRepository.downloadDocument(documentId)) {
                is ApiResult.Success -> {
                    _effects.emit(UploadedAnalysesEffect.OpenFile(result.data))
                }
                is ApiResult.HttpError, is ApiResult.NetworkError -> {
                    _refreshErrors.emit(result.toUserMessage())
                }
                is ApiResult.AuthError -> {
                    _refreshErrors.emit(result.toUserMessage())
                }
            }
        }
    }

    // ── Data Refresh ──────────────────────────────────────────────────────

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
