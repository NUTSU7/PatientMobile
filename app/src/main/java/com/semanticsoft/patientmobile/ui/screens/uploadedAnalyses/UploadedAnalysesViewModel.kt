package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.BuildConfig
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.SyncStatus
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val DEMO_DOCUMENTS = listOf(
    PatientDocument(
        id = "demo-doc-1",
        ownerUserId = "demo-user",
        originalFileName = "Analize Sânge 2024.pdf",
        mimeType = "application/pdf",
        fileSizeBytes = 1_245_338,
        uploadedAt = Instant.parse("2024-07-11T13:23:00Z"),
        syncStatus = SyncStatus.SYNCED
    ),
    PatientDocument(
        id = "demo-doc-2",
        ownerUserId = "demo-user",
        originalFileName = "Ecografie Abdominală.pdf",
        mimeType = "application/pdf",
        fileSizeBytes = 2_890_112,
        uploadedAt = Instant.parse("2024-08-15T10:15:00Z"),
        syncStatus = SyncStatus.SYNCED
    ),
    PatientDocument(
        id = "demo-doc-3",
        ownerUserId = "demo-user",
        originalFileName = "Rezultate Radiografie Torace.pdf",
        mimeType = "application/pdf",
        fileSizeBytes = 3_450_000,
        uploadedAt = Instant.parse("2024-09-20T16:45:00Z"),
        syncStatus = SyncStatus.SYNCED
    ),
    PatientDocument(
        id = "demo-doc-4",
        ownerUserId = "demo-user",
        originalFileName = "Radiografie pulmonara.jpg",
        mimeType = "image/jpeg",
        fileSizeBytes = 1_800_000,
        uploadedAt = Instant.parse("2024-10-05T09:30:00Z"),
        syncStatus = SyncStatus.SYNCED
    ),
    PatientDocument(
        id = "demo-doc-5",
        ownerUserId = "demo-user",
        originalFileName = "Consultatie cardiologie.png",
        mimeType = "image/png",
        fileSizeBytes = 756_000,
        uploadedAt = Instant.parse("2024-10-12T14:00:00Z"),
        syncStatus = SyncStatus.SYNCED
    ),
    PatientDocument(
        id = "demo-doc-6",
        ownerUserId = "demo-user",
        originalFileName = "Analize laborator martie.jpg",
        mimeType = "image/jpg",
        fileSizeBytes = 420_000,
        uploadedAt = Instant.parse("2025-03-10T08:45:00Z"),
        syncStatus = SyncStatus.SYNCED
    )
)

data class UploadedAnalysesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allDocuments: List<PatientDocument> = emptyList()
) {
    val documents: List<PatientDocument>
        get() = when {
            allDocuments.isNotEmpty() -> allDocuments
            BuildConfig.DEBUG -> DEMO_DOCUMENTS
            else -> emptyList()
        }

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

    private val _stateFlow = MutableStateFlow(UploadedAnalysesUiState(isLoading = true))
    val stateFlow: StateFlow<UploadedAnalysesUiState> = _stateFlow.asStateFlow()

    init {
        refreshDocuments()
    }

    fun refreshDocuments() {
        viewModelScope.launch {
            _stateFlow.value = _stateFlow.value.copy(isLoading = true, errorMessage = null)
            when (val result = documentRepository.getDocuments().first { it !is Resource.Loading }) {
                is Resource.Error -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Success -> {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        allDocuments = result.data
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }
}
