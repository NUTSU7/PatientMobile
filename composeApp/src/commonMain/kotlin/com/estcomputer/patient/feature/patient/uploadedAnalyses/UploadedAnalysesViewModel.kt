package com.estcomputer.patient.feature.patient.uploadedAnalyses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estcomputer.patient.feature.patient.uploadedAnalyses.components.SortCriteria
import com.estcomputer.patient.feature.patient.uploadedAnalyses.components.SortOrder
import java.io.File
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

class UploadedAnalysesViewModel : ViewModel() {

    private val _state = MutableStateFlow(UploadedAnalysesUiState())
    val state: StateFlow<UploadedAnalysesUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    private val _effects = MutableSharedFlow<UploadedAnalysesEffect>()
    val effects: SharedFlow<UploadedAnalysesEffect> = _effects.asSharedFlow()

    fun onSortCriteriaSelected(criteria: SortCriteria) {
        _state.update { it.copy(sortCriteria = criteria) }
    }

    fun onSortOrderSelected(order: SortOrder) {
        _state.update { it.copy(sortOrder = order) }
    }

    fun onShowRenameDialog(documentId: String) {
        _state.update { it.copy(dialogState = DialogState.Rename(documentId)) }
    }

    fun onDismissRenameDialog() {
        _state.update { it.copy(dialogState = DialogState.None) }
    }

    fun onRenameDocument(documentId: String, newName: String) {
        _state.update { current ->
            val updated = current.allDocuments.map {
                if (it.id == documentId) it.copy(originalFileName = newName) else it
            }
            current.copy(allDocuments = updated, dialogState = DialogState.None)
        }
    }

    fun onShowDeleteDialog(documentId: String) {
        _state.update { it.copy(dialogState = DialogState.SingleDelete(documentId)) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(dialogState = DialogState.None) }
    }

    fun onDeleteDocument(documentId: String) {
        _state.update { current ->
            current.copy(
                allDocuments = current.allDocuments.filterNot { it.id == documentId },
                dialogState = DialogState.None
            )
        }
    }

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

    fun onShowBulkDeleteDialog(ids: Set<String>) {
        if (ids.isEmpty()) return
        _state.update { it.copy(dialogState = DialogState.BulkDelete(ids)) }
    }

    fun onDismissBulkDeleteDialog() {
        _state.update { it.copy(dialogState = DialogState.None) }
    }

    fun onBulkDelete() {
        val bulkState = _state.value.dialogState as? DialogState.BulkDelete ?: return
        val ids = bulkState.documentIds
        _state.update { current ->
            current.copy(
                allDocuments = current.allDocuments.filterNot { it.id in ids },
                isSelectionMode = false,
                selectedDocumentIds = emptySet(),
                dialogState = DialogState.None
            )
        }
    }

    fun onDownloadDocument(documentId: String) {
        val doc = _state.value.allDocuments.firstOrNull { it.id == documentId }
        doc?.localFilePath?.let { path ->
            viewModelScope.launch {
                _effects.emit(UploadedAnalysesEffect.OpenFile(File(path)))
            }
        }
    }

    fun refreshDocuments() {
        // Default no-op
    }
}
