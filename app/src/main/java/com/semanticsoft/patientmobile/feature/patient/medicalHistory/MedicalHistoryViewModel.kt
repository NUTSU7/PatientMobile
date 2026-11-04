package com.semanticsoft.patientmobile.feature.patient.medicalHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.patient.DoseUnit
import com.semanticsoft.patientmobile.domain.model.patient.MealRelation
import com.semanticsoft.patientmobile.domain.model.patient.Medication
import com.semanticsoft.patientmobile.domain.model.patient.MedicationDraft
import com.semanticsoft.patientmobile.domain.model.patient.MedicationOcrItem
import com.semanticsoft.patientmobile.domain.model.patient.MedicationSchedule
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import com.semanticsoft.patientmobile.domain.model.patient.PersonalNote
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.common.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalHistoryRepository
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.patient.OcrRepository
import com.semanticsoft.patientmobile.domain.usecase.patient.BuildTreatmentResult
import com.semanticsoft.patientmobile.domain.usecase.patient.BuildTreatmentUseCase
import com.semanticsoft.patientmobile.domain.model.patient.OcrStatus
import com.semanticsoft.patientmobile.domain.model.patient.MedicationOcrDraft
import com.semanticsoft.patientmobile.domain.model.patient.PersonalNoteOcrDraft
import com.semanticsoft.patientmobile.feature.patient.upload.ImageNormalizer
import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.core.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class MedicalHistoryEvent {
    data class NavigateToReportResults(val reportId: String) : MedicalHistoryEvent()
}

enum class MedicineIconType {
    PILL,
    CAPSULE,
    SPRAY,
    SYRUP,
    INJECTION,
    DROPS
}

enum class CombinedSectionTab {
    MEDICINES,
    NOTES
}

enum class DocumentReadiness { Checking, Pending, Ready, Failed, Unavailable }

data class MedicineItem(
    val id: String,
    val name: String,
    val doseValue: Double? = null,
    val doseUnitLabel: String? = null,
    val doseUnit: DoseUnit? = null,
    val strength: String? = null,
    val frequencyText: String? = null,
    val treatmentId: String? = null,
    val treatmentName: String? = null,
    val schedules: List<MedicineScheduleUiItem>,
    val daysRemaining: Int,
    val iconType: MedicineIconType = MedicineIconType.PILL,
    val analysisDocumentId: String? = null,
    val analysisDocumentIds: List<String> = emptyList(),
    val effectiveDate: java.time.LocalDate? = null,
    val endDate: java.time.LocalDate? = null,
    val active: Boolean = true,
    val createdAt: java.time.Instant? = null,
    val attachmentIds: List<String> = emptyList(),
    val ocrReviewConfirmed: Boolean = false
)

/** Entry of the medicines list: a standalone medication or a collapsible treatment group. */
sealed interface MedicineListEntry {
    val key: String

    data class Single(val item: MedicineItem) : MedicineListEntry {
        override val key: String get() = item.id
    }

    data class Treatment(
        val treatmentId: String,
        val name: String,
        val items: List<MedicineItem>
    ) : MedicineListEntry {
        override val key: String get() = "treatment:$treatmentId"
    }
}

data class MedicineScheduleUiItem(
    val administrationTime: String,
    val mealRelationLabel: String?,
    val mealRelation: MealRelation = MealRelation.NO_MEAL_RELATION
)

enum class NoteSeverity {
    GOOD,
    OK,
    BAD
}

data class PersonalNoteItem(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val dateLabel: String,
    val severity: NoteSeverity,
    val accentColor: androidx.compose.ui.graphics.Color? = null,
    val analysisDocumentId: String? = null,
    val analysisDocumentIds: List<String> = emptyList(),
    val noteDate: java.time.LocalDate = java.time.LocalDate.now(),
    val createdAt: java.time.Instant? = null,
    val attachmentIds: List<String> = emptyList()
)

data class MedicalHistoryUiState(
    val greetingName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allDocuments: List<PatientDocument> = emptyList(),
    val documentReadiness: Map<String, DocumentReadiness> = emptyMap(),
    val readinessCheckInProgress: Boolean = false,
    val availableYears: List<Int> = emptyList(),
    val documentDisplayNames: Map<String, String> = emptyMap(),
    val selectedYear: Int? = null,
    val selectedDate: LocalDate? = null,
    val medicines: List<MedicineItem> = emptyList(),
    val notes: List<PersonalNoteItem> = emptyList(),
    val medicationDialogTargetId: String? = null,
    val noteDialogTargetId: String? = null,
    val medicationDialogError: String? = null,
    val medicationDialogTreatmentId: String? = null,
    val medicationSaving: Boolean = false,
    val treatmentName: String = "",
    val medicationDrafts: List<MedicationDraft> = emptyList(),
    val expandedMedicationDraftId: String? = null,
    val medicationAnalysisDocumentIds: Set<String> = emptySet(),
    val treatmentExpanded: Map<String, Boolean> = emptyMap(),
    val deleteTreatmentTargetId: String? = null,
    val noteDialogError: String? = null,
    val deleteMedicationTargetId: String? = null,
    val deleteNoteTargetId: String? = null,
    val selectedCombinedSectionTab: CombinedSectionTab = CombinedSectionTab.NOTES,
    val medicationOcrDraftResult: MedicationOcrDraft? = null,
    val medicationOcrFeedback: String? = null,
    val medicationOcrFeedbackError: Boolean = false,
    val medicationOcrExtracting: Boolean = false,
    val noteOcrDraftResult: PersonalNoteOcrDraft? = null,
    val noteOcrFeedback: String? = null,
    val noteOcrFeedbackError: Boolean = false,
    val noteOcrExtracting: Boolean = false,
    val ocrMedicationAttachmentIds: List<String> = emptyList(),
    val ocrNoteAttachmentIds: List<String> = emptyList(),
    val reviewDialogDocumentId: String? = null,
    val reviewDialogReportId: String? = null,
    val reviewDialogDisplayTitle: String = "",
    val reviewDialogDisplaySubtitle: String = "",
    val reviewDialogSourceLaboratory: String = "",
    val reviewDialogObservedAt: java.time.LocalDate? = null,
    val reviewDialogIsLoading: Boolean = false,
    val reviewDialogError: String? = null,
    val documentReportCounts: Map<String, Int> = emptyMap(),
    val retryDialogDocumentId: String? = null
) {
    val showAddMedicationDialog: Boolean
        get() = medicationDialogTargetId != null

    val showAddNoteDialog: Boolean
        get() = noteDialogTargetId != null

    val showDeleteMedicationDialog: Boolean
        get() = deleteMedicationTargetId != null || deleteTreatmentTargetId != null

    /** True when the dialog edits an existing medication or treatment instead of creating one. */
    val isEditingMedicationDialog: Boolean
        get() = medicationDialogTargetId != null && medicationDialogTargetId != ADD_TARGET_ID

    /** Only a standalone medication (not part of a treatment) is edited with a single card. */
    val isEditingStandaloneMedication: Boolean
        get() = isEditingMedicationDialog && medicationDialogTreatmentId == null

    /** Drafts starting more than 14 days after a selected analysis (informative note, like on web). */
    val lateStartDraftIds: Set<String>
        get() {
            val analysisDates = allDocuments
                .filter { it.id in medicationAnalysisDocumentIds }
                .map { (it.observedAt ?: it.uploadedAt).atZone(ZoneId.systemDefault()).toLocalDate() }
            if (analysisDates.isEmpty()) return emptySet()
            return medicationDrafts
                .filter { draft -> analysisDates.any { ChronoUnit.DAYS.between(it, draft.startDate) > LATE_START_DAYS } }
                .map { it.draftId }
                .toSet()
        }

    val medicineEntries: List<MedicineListEntry>
        get() {
            val entries = mutableListOf<MedicineListEntry>()
            val groupIndex = mutableMapOf<String, Int>()
            filteredMedicines.forEach { item ->
                val treatmentId = item.treatmentId
                if (treatmentId == null) {
                    entries.add(MedicineListEntry.Single(item))
                } else {
                    val index = groupIndex[treatmentId]
                    if (index == null) {
                        groupIndex[treatmentId] = entries.size
                        entries.add(
                            MedicineListEntry.Treatment(
                                treatmentId = treatmentId,
                                name = item.treatmentName ?: item.name,
                                items = listOf(item)
                            )
                        )
                    } else {
                        val group = entries[index] as MedicineListEntry.Treatment
                        entries[index] = group.copy(items = group.items + item)
                    }
                }
            }
            return entries
        }

    val showDeleteNoteDialog: Boolean
        get() = deleteNoteTargetId != null

    val showReviewDialog: Boolean
        get() = reviewDialogDocumentId != null

    val showRetryDialog: Boolean
        get() = retryDialogDocumentId != null

    val analysisDocuments: List<PatientDocument>
        get() = allDocuments

    val isDateFilterActive: Boolean
        get() = selectedYear != null || selectedDate != null
    val visibleDocumentIds: Set<String>
        get() = filteredDocuments.map { it.id }.toSet()
    val filteredMedicines: List<MedicineItem>
        get() {
            val docIds = visibleDocumentIds
            return medicines.filter { med ->
                if (!isDateFilterActive) return@filter true
                val medDocIds = med.analysisDocumentIds.ifEmpty { listOfNotNull(med.analysisDocumentId) }
                if (medDocIds.isEmpty()) return@filter false
                medDocIds.any { it in docIds }
            }
        }
    val filteredNotes: List<PersonalNoteItem>
        get() {
            val docIds = visibleDocumentIds
            return notes.filter { note ->
                if (!isDateFilterActive) return@filter true
                val noteDocIds = note.analysisDocumentIds.ifEmpty { listOfNotNull(note.analysisDocumentId) }
                if (noteDocIds.isEmpty()) return@filter false
                noteDocIds.any { it in docIds }
            }
        }

    val observedDatesForSelectedYear: List<LocalDate>
        get() = if (selectedYear == null) emptyList() else analysisDocuments
            .mapNotNull { doc ->
                val instant = doc.observedAt ?: doc.uploadedAt
                val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                if (date.year == selectedYear) date else null
            }
            .distinct()
            .sortedDescending()

    val filteredDocuments: List<PatientDocument>
        get() = analysisDocuments.filter { document ->
            val obsInstant = document.observedAt ?: document.uploadedAt
            val zonedDate = obsInstant.atZone(ZoneId.systemDefault())
            if (selectedYear != null && zonedDate.year != selectedYear) return@filter false
            selectedDate == null || zonedDate.toLocalDate() == selectedDate
        }.sortedByDescending { it.observedAt ?: it.uploadedAt }
}

private const val ADD_TARGET_ID = "add"
private const val LATE_START_DAYS = 14L

@HiltViewModel
class MedicalHistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val medicalResultRepository: MedicalResultRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val ocrRepository: OcrRepository,
    private val globalSyncManager: GlobalSyncManager,
    private val buildTreatment: BuildTreatmentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MedicalHistoryUiState(isLoading = true))
    val state: StateFlow<MedicalHistoryUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    private val _events = MutableSharedFlow<MedicalHistoryEvent>()
    val events: SharedFlow<MedicalHistoryEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            globalSyncManager.syncEvents.collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val hasData = _state.value.allDocuments.isNotEmpty()
            if (!hasData) {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
            }

            var errorMessage: String? = null

            val userDeferred = async { authRepository.getCurrentUser() }

            val documents = when (val result = documentRepository.getDocuments(0, 50, sortBy = "uploadedAt", sortDir = "desc")) {
                is ApiResult.Success -> result.data
                is ApiResult.HttpError -> {
                    errorMessage = result.toUserMessage()
                    emptyList()
                }
                is ApiResult.NetworkError -> {
                    errorMessage = result.toUserMessage()
                    emptyList()
                }
                is ApiResult.AuthError -> {
                    errorMessage = result.toUserMessage()
                    emptyList()
                }
            }

            val keepExisting = errorMessage != null && hasData
            val effectiveDocuments = if (keepExisting) _state.value.allDocuments else documents
            if (keepExisting) {
                _refreshErrors.emit(errorMessage!!)
                errorMessage = null
            }

            launch {
                checkDocumentReadiness(effectiveDocuments)
            }

            val years = effectiveDocuments
                .mapNotNull { it.observedAt ?: it.uploadedAt }
                .map { it.atZone(ZoneId.systemDefault()).year }
                .distinct()
                .sortedDescending()
            val selectedYear = when {
                _state.value.selectedYear == null -> null
                years.contains(_state.value.selectedYear) -> _state.value.selectedYear
                years.contains(LocalDate.now().year) -> LocalDate.now().year
                years.isNotEmpty() -> years.first()
                else -> null
            }
            val selectedDate = _state.value.selectedDate?.takeIf { date ->
                effectiveDocuments.any { doc ->
                    (doc.observedAt ?: doc.uploadedAt).atZone(ZoneId.systemDefault()).toLocalDate() == date
                }
            } ?: if (selectedYear != null && _state.value.selectedDate == null) {
                effectiveDocuments
                    .mapNotNull { (it.observedAt ?: it.uploadedAt).atZone(ZoneId.systemDefault()).toLocalDate() }
                    .filter { it.year == selectedYear }
                    .distinct()
                    .sortedDescending()
                    .firstOrNull()
            } else null

            val medicines = when (val result = medicalHistoryRepository.getMedications()) {
                is ApiResult.Success -> {
                    val items = result.data
                        .sortedByDescending { med ->
                            med.effectiveDate ?: med.endDate ?: LocalDate.EPOCH
                        }
                        .map { it.toUiItem() }
                    items
                }
                else -> {
                    val msg = result.toUserMessage()
                    _state.value.medicines
                }
            }

            val notes = when (val result = medicalHistoryRepository.getNotes()) {
                is ApiResult.Success -> {
                    val items = result.data
                        .sortedByDescending { it.noteDate }
                        .map { it.toUiItem() }
                    items
                }
                else -> {
                    val msg = result.toUserMessage()
                    _state.value.notes
                }
            }

            val userResult = userDeferred.await()
            if (userResult is ApiResult.Success) {
                val user = userResult.data
                val greetingName = user.firstName.ifBlank { user.email.substringBefore("@") }
                _state.update { it.copy(greetingName = greetingName) }
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = errorMessage,
                    allDocuments = effectiveDocuments,
                    availableYears = years,
                    selectedYear = selectedYear,
                    selectedDate = selectedDate,
                    medicines = medicines,
                    notes = notes
                )
            }
        }
    }

    fun onDocumentSelected(documentId: String) {
        if (_state.value.documentReadiness[documentId] == DocumentReadiness.Failed) {
            _state.update { it.copy(retryDialogDocumentId = documentId) }
            return
        }
        viewModelScope.launch {
            val extractionsResult = ocrRepository.listExtractions(documentId)
            if (extractionsResult !is ApiResult.Success) {
                _refreshErrors.emit("Nu s-au putut \u00EEnc\u0103rca rezultatele.")
                return@launch
            }
            val successRun = extractionsResult.data.firstOrNull {
                it.status == OcrStatus.COMPLETED || it.status == OcrStatus.SUCCESS || it.status == OcrStatus.PARTIAL
            }
            if (successRun == null) {
                _refreshErrors.emit("Analiza \u00EEnc\u0103 nu a fost procesat\u0103.")
                return@launch
            }
            val extractionDetail = ocrRepository.getExtractionStatus(documentId, successRun.id)
            if (extractionDetail !is ApiResult.Success) {
                _refreshErrors.emit("Nu s-au putut \u00EEnc\u0103rca detaliile extrac\u021Biei.")
                return@launch
            }
            val firstReport = extractionDetail.data.reports.firstOrNull()
            if (firstReport == null) {
                _refreshErrors.emit("Nu exist\u0103 rapoarte disponibile.")
                return@launch
            }
            _events.emit(MedicalHistoryEvent.NavigateToReportResults(firstReport.id))
        }
    }

    fun onDismissRetryDialog() {
        _state.update { it.copy(retryDialogDocumentId = null) }
    }

    fun onRetryExtraction() {
        val documentId = _state.value.retryDialogDocumentId ?: return
        _state.update {
            val readiness = it.documentReadiness.toMutableMap()
            readiness[documentId] = DocumentReadiness.Checking
            it.copy(retryDialogDocumentId = null, documentReadiness = readiness)
        }
        viewModelScope.launch {
            when (val result = ocrRepository.startExtraction(documentId)) {
                is ApiResult.Success -> {
                    globalSyncManager.triggerSync()
                    refresh()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _refreshErrors.emit(result.toUserMessage())
                    refresh()
                }
            }
        }
    }

    fun onYearSelected(year: Int?) {
        _state.update { current ->
            val updated = current.copy(selectedYear = year)
            updated.copy(
                selectedDate = if (year != null) updated.observedDatesForSelectedYear.firstOrNull() else null
            )
        }
    }

    fun onDateSelected(date: LocalDate) {
        _state.update { current ->
            if (current.selectedDate == date) {
                current.copy(selectedDate = null)
            } else {
                current.copy(selectedDate = date)
            }
        }
    }

    fun onCombinedSectionTabSelected(tab: CombinedSectionTab) {
        _state.update { it.copy(selectedCombinedSectionTab = tab) }
    }

    fun onShowAddMedicationDialog() {
        val initialDraft = newMedicationDraft()
        _state.update {
            it.copy(
                medicationDialogTargetId = ADD_TARGET_ID,
                medicationDialogTreatmentId = null,
                medicationDialogError = null,
                treatmentName = "",
                medicationDrafts = listOf(initialDraft),
                expandedMedicationDraftId = initialDraft.draftId,
                medicationAnalysisDocumentIds = emptySet(),
                ocrMedicationAttachmentIds = emptyList()
            )
        }
    }

    /** Opens the dialog for a standalone medication, or for its whole treatment when it belongs to one. */
    fun onShowEditMedicationDialog(medicationId: String) {
        val current = _state.value
        val medication = current.medicines.find { it.id == medicationId } ?: return
        val treatmentId = medication.treatmentId
        val members = if (treatmentId != null) {
            current.medicines.filter { it.treatmentId == treatmentId }
        } else {
            listOf(medication)
        }
        _state.update {
            it.copy(
                medicationDialogTargetId = treatmentId ?: medication.id,
                medicationDialogTreatmentId = treatmentId,
                medicationDialogError = null,
                treatmentName = medication.treatmentName ?: medication.name,
                medicationDrafts = members.map { member -> member.toDraft() },
                expandedMedicationDraftId = null,
                medicationAnalysisDocumentIds = members
                    .flatMap { member -> member.analysisDocumentIds.ifEmpty { listOfNotNull(member.analysisDocumentId) } }
                    .toSet(),
                ocrMedicationAttachmentIds = members.flatMap { member -> member.attachmentIds }.distinct()
            )
        }
    }

    fun onDismissAddMedicationDialog() {
        _state.update {
            it.copy(
                medicationDialogTargetId = null,
                medicationDialogTreatmentId = null,
                medicationDialogError = null,
                medicationSaving = false,
                treatmentName = "",
                medicationDrafts = emptyList(),
                expandedMedicationDraftId = null,
                medicationAnalysisDocumentIds = emptySet()
            )
        }
        clearMedicationOcrDraft()
    }

    fun onTreatmentNameChange(name: String) {
        _state.update { it.copy(treatmentName = name) }
    }

    fun onAnalysisDocumentsChange(ids: Set<String>) {
        _state.update { current ->
            val firstId = ids.firstOrNull { it.isNotEmpty() }
            val firstDraft = current.medicationDrafts.singleOrNull()
            val suggestedName = if (firstId != null && current.treatmentName.isBlank() && firstDraft?.name.isNullOrBlank()) {
                current.allDocuments.find { it.id == firstId }?.originalFileName
            } else null
            current.copy(
                medicationAnalysisDocumentIds = ids.filter { it.isNotEmpty() }.toSet(),
                treatmentName = suggestedName ?: current.treatmentName
            )
        }
    }

    fun onAddMedicationDraft() {
        val newDraft = newMedicationDraft()
        _state.update {
            it.copy(
                medicationDrafts = it.medicationDrafts + newDraft,
                expandedMedicationDraftId = newDraft.draftId
            )
        }
    }

    fun onToggleMedicationDraft(draftId: String) {
        _state.update { current ->
            val nextId = if (current.expandedMedicationDraftId == draftId) null else draftId
            current.copy(expandedMedicationDraftId = nextId)
        }
    }

    fun onRemoveMedicationDraft(draftId: String) {
        _state.update { current ->
            if (current.medicationDrafts.size <= 1) current
            else {
                val remaining = current.medicationDrafts.filterNot { it.draftId == draftId }
                val nextExpanded = if (current.expandedMedicationDraftId == draftId) {
                    remaining.lastOrNull()?.draftId
                } else {
                    current.expandedMedicationDraftId
                }
                current.copy(
                    medicationDrafts = remaining,
                    expandedMedicationDraftId = nextExpanded
                )
            }
        }
    }

    fun onUpdateMedicationDraft(draftId: String, transform: (MedicationDraft) -> MedicationDraft) {
        _state.update { current ->
            current.copy(
                medicationDrafts = current.medicationDrafts.map { if (it.draftId == draftId) transform(it) else it }
            )
        }
    }

    fun onShowAddNoteDialog() {
        _state.update { it.copy(noteDialogTargetId = "add", noteDialogError = null) }
    }

    fun onShowEditNoteDialog(noteId: String) {
        _state.update { it.copy(noteDialogTargetId = noteId, noteDialogError = null) }
    }

    fun onDismissAddNoteDialog() {
        _state.update { it.copy(noteDialogTargetId = null, noteDialogError = null) }
        clearNoteOcrDraft()
    }

    fun extractMedicationFromFile(filePath: String) {
        val normalizedPath = normalizeIfNeeded(filePath)
        _state.update { it.copy(medicationOcrExtracting = true, medicationOcrFeedback = null, medicationOcrFeedbackError = false) }
        viewModelScope.launch {
            when (val result = medicalHistoryRepository.extractMedicationOcrDraft(normalizedPath)) {
                is ApiResult.Success -> {
                    val draft = result.data
                    val attachmentIds = _state.value.ocrMedicationAttachmentIds + listOf(draft.attachment.id)
                    _state.update { current ->
                        val today = LocalDate.now()
                        val keptDrafts = current.medicationDrafts.filter { !it.id.isNullOrBlank() || it.name.isNotBlank() }
                        val ocrDrafts = draft.items.map { item -> item.toDraft(today) }
                        val nextExpanded = ocrDrafts.firstOrNull()?.draftId ?: current.expandedMedicationDraftId
                        current.copy(
                            medicationOcrDraftResult = draft,
                            medicationOcrExtracting = false,
                            medicationOcrFeedback = "Sugestiile OCR au fost aplicate \u00EEn c\u00E2mpuri.",
                            medicationOcrFeedbackError = false,
                            ocrMedicationAttachmentIds = attachmentIds,
                            medicationDrafts = keptDrafts + ocrDrafts,
                            expandedMedicationDraftId = nextExpanded,
                            treatmentName = current.treatmentName.ifBlank { draft.suggestedTreatmentName.orEmpty() }
                        )
                    }
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _state.update {
                        it.copy(
                            medicationOcrExtracting = false,
                            medicationOcrFeedback = result.toUserMessage(),
                            medicationOcrFeedbackError = true
                        )
                    }
                }
            }
        }
    }

    fun extractPersonalNoteFromFile(filePath: String) {
        val normalizedPath = normalizeIfNeeded(filePath)
        _state.update { it.copy(noteOcrExtracting = true, noteOcrFeedback = null, noteOcrFeedbackError = false) }
        viewModelScope.launch {
            when (val result = medicalHistoryRepository.extractPersonalNoteOcrDraft(normalizedPath)) {
                is ApiResult.Success -> {
                    val draft = result.data
                    val attachmentIds = _state.value.ocrNoteAttachmentIds + listOf(draft.attachment.id)
                    _state.update {
                        it.copy(
                            noteOcrDraftResult = draft,
                            noteOcrExtracting = false,
                            noteOcrFeedback = "Textul extras a fost aplicat \u00EEn c\u00E2mpuri.",
                            noteOcrFeedbackError = false,
                            ocrNoteAttachmentIds = attachmentIds
                        )
                    }
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _state.update {
                        it.copy(
                            noteOcrExtracting = false,
                            noteOcrFeedback = result.toUserMessage(),
                            noteOcrFeedbackError = true
                        )
                    }
                }
            }
        }
    }

    fun clearMedicationOcrDraft() {
        _state.update {
            it.copy(
                medicationOcrDraftResult = null,
                medicationOcrFeedback = null,
                medicationOcrFeedbackError = false,
                medicationOcrExtracting = false,
                ocrMedicationAttachmentIds = emptyList()
            )
        }
    }

    fun clearNoteOcrDraft() {
        _state.update {
            it.copy(
                noteOcrDraftResult = null,
                noteOcrFeedback = null,
                noteOcrFeedbackError = false,
                noteOcrExtracting = false,
                ocrNoteAttachmentIds = emptyList()
            )
        }
    }

    /** Validates the cards and saves them: new treatment, edited treatment, or edited standalone medication. */
    fun saveTreatment() {
        val current = _state.value
        if (current.medicationSaving) return
        val attachmentIds = current.ocrMedicationAttachmentIds.distinct()
        val result = buildTreatment(
            treatmentName = current.treatmentName,
            drafts = current.medicationDrafts,
            analysisDocumentIds = current.medicationAnalysisDocumentIds.toList(),
            attachmentIds = attachmentIds
        )
        val treatment = when (result) {
            is BuildTreatmentResult.Invalid -> {
                _state.update { it.copy(medicationDialogError = result.message) }
                return
            }
            is BuildTreatmentResult.Valid -> result.treatment
        }
        _state.update { it.copy(medicationSaving = true, medicationDialogError = null) }
        viewModelScope.launch {
            val treatmentId = current.medicationDialogTreatmentId
            val apiResult: ApiResult<*> = when {
                treatmentId != null -> medicalHistoryRepository.updateTreatment(treatmentId, treatment)
                current.isEditingStandaloneMedication -> {
                    val targetId = current.medicationDialogTargetId.orEmpty()
                    val existing = current.medicines.find { it.id == targetId }
                    val updated = treatment.medications.first().copy(
                        id = targetId,
                        active = existing?.active ?: true,
                        attachmentIds = attachmentIds
                    )
                    medicalHistoryRepository.updateMedication(targetId, updated)
                }
                else -> medicalHistoryRepository.createTreatment(treatment)
            }
            when (apiResult) {
                is ApiResult.Success -> {
                    globalSyncManager.triggerSync()
                    onDismissAddMedicationDialog()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _state.update {
                        it.copy(medicationSaving = false, medicationDialogError = apiResult.toUserMessage())
                    }
                }
            }
        }
    }

    fun addNote(
        title: String,
        doctorLocation: String,
        content: String,
        analysisDocumentIds: List<String> = emptyList()
    ) {
        val cleanTitle = title.trim().replace(Regex("\\s+"), " ")
        if (cleanTitle.isBlank()) {
            _state.update { it.copy(noteDialogError = "Titlul notiței este obligatoriu.") }
            return
        }
        val cleanDoctorLocation = doctorLocation.trim().replace(Regex("\\s+"), " ")
        val cleanContent = content.trim().replace(Regex("\\s+"), " ")

        val targetId = _state.value.noteDialogTargetId
        val isEditing = targetId != null && targetId != "add"
        viewModelScope.launch {
            val existing = if (isEditing) _state.value.notes.find { it.id == targetId } else null
            val primaryDocId = analysisDocumentIds.firstOrNull()
            val note = PersonalNote(
                id = if (isEditing) targetId!! else "",
                analysisName = cleanTitle,
                doctorLocation = cleanDoctorLocation.ifBlank { null },
                clinicalObservations = cleanContent.ifBlank { null },
                noteDate = existing?.noteDate ?: LocalDate.now(),
                analysisDocumentId = primaryDocId,
                analysisDocumentIds = analysisDocumentIds,
                attachmentIds = (existing?.attachmentIds ?: emptyList()) + _state.value.ocrNoteAttachmentIds,
                createdAt = existing?.createdAt
            )
            if (isEditing) {
                when (val result = medicalHistoryRepository.updateNote(targetId!!, note)) {
                    is ApiResult.Success -> {
                        globalSyncManager.triggerSync()
                        onDismissAddNoteDialog()
                    }
                    is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                        _state.update { it.copy(noteDialogError = result.toUserMessage()) }
                    }
                }
            } else {
                when (val result = medicalHistoryRepository.createNote(note)) {
                    is ApiResult.Success -> {
                        globalSyncManager.triggerSync()
                        onDismissAddNoteDialog()
                    }
                    is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                        _state.update { it.copy(noteDialogError = result.toUserMessage()) }
                    }
                }
            }
        }
    }

    fun onShowDeleteMedicationDialog(medicationId: String) {
        _state.update { it.copy(deleteMedicationTargetId = medicationId) }
    }

    fun onDismissDeleteMedicationDialog() {
        _state.update { it.copy(deleteMedicationTargetId = null) }
    }

    fun onDeleteMedication(medicationId: String) {
        viewModelScope.launch {
            when (val result = medicalHistoryRepository.deleteMedication(medicationId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleteMedicationTargetId = null) }
                    globalSyncManager.triggerSync()
                    refresh()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _refreshErrors.emit(result.toUserMessage())
                    _state.update { it.copy(deleteMedicationTargetId = null) }
                }
            }
        }
    }

    fun onToggleTreatmentExpanded(treatmentId: String, expanded: Boolean) {
        _state.update { it.copy(treatmentExpanded = it.treatmentExpanded + (treatmentId to expanded)) }
    }

    fun onShowDeleteTreatmentDialog(treatmentId: String) {
        _state.update { it.copy(deleteTreatmentTargetId = treatmentId) }
    }

    fun onDismissDeleteTreatmentDialog() {
        _state.update { it.copy(deleteTreatmentTargetId = null) }
    }

    fun onDeleteTreatment(treatmentId: String) {
        viewModelScope.launch {
            when (val result = medicalHistoryRepository.deleteTreatment(treatmentId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            deleteTreatmentTargetId = null,
                            treatmentExpanded = it.treatmentExpanded - treatmentId
                        )
                    }
                    globalSyncManager.triggerSync()
                    refresh()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _refreshErrors.emit(result.toUserMessage())
                    _state.update { it.copy(deleteTreatmentTargetId = null) }
                }
            }
        }
    }

    fun onShowDeleteNoteDialog(noteId: String) {
        _state.update { it.copy(deleteNoteTargetId = noteId) }
    }

    fun onDismissDeleteNoteDialog() {
        _state.update { it.copy(deleteNoteTargetId = null) }
    }

    fun onDeleteNote(noteId: String) {
        viewModelScope.launch {
            when (val result = medicalHistoryRepository.deleteNote(noteId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleteNoteTargetId = null) }
                    globalSyncManager.triggerSync()
                    refresh()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _refreshErrors.emit(result.toUserMessage())
                    _state.update { it.copy(deleteNoteTargetId = null) }
                }
            }
        }
    }

    fun onReviewClick(documentId: String) {
        val docName = state.value.allDocuments.find { it.id == documentId }?.originalFileName
        val defaultTitle = docName?.let { name ->
            val dotIndex = name.lastIndexOf('.')
            if (dotIndex > 0) name.substring(0, dotIndex) else name
        }
        _state.update {
            it.copy(
                reviewDialogDocumentId = documentId,
                reviewDialogIsLoading = true,
                reviewDialogError = null,
                reviewDialogDisplayTitle = defaultTitle ?: "",
                reviewDialogDisplaySubtitle = "",
                reviewDialogSourceLaboratory = "",
                reviewDialogObservedAt = null
            )
        }
        viewModelScope.launch {
            val extractionsResult = ocrRepository.listExtractions(documentId)
            if (extractionsResult !is ApiResult.Success) {
                _state.update { it.copy(reviewDialogIsLoading = false, reviewDialogError = "Nu s-au putut \u00EEnc\u0103rca detaliile.") }
                return@launch
            }
            val successRun = extractionsResult.data.firstOrNull {
                it.status == OcrStatus.COMPLETED || it.status == OcrStatus.SUCCESS || it.status == OcrStatus.PARTIAL
            }
            if (successRun == null) {
                _state.update { it.copy(reviewDialogIsLoading = false, reviewDialogError = "Analiza \u00EEnc\u0103 nu a fost procesat\u0103.") }
                return@launch
            }
            val detailResult = ocrRepository.getExtractionStatus(documentId, successRun.id)
            if (detailResult !is ApiResult.Success || detailResult.data.reports.isEmpty()) {
                _state.update { it.copy(reviewDialogIsLoading = false, reviewDialogError = "Nu exist\u0103 rapoarte disponibile.") }
                return@launch
            }
            val reportId = detailResult.data.reports.first().id

            when (val reportResult = medicalResultRepository.getReportDetails(reportId)) {
                is ApiResult.Success -> {
                    val report = reportResult.data
                    _state.update {
                        it.copy(
                            reviewDialogReportId = reportId,
                            reviewDialogDisplayTitle = report.displayTitle
                                ?: it.reviewDialogDisplayTitle,
                            reviewDialogDisplaySubtitle = report.displaySubtitle ?: "",
                            reviewDialogSourceLaboratory = report.sourceLaboratory ?: "",
                            reviewDialogObservedAt = report.observedAt,
                            reviewDialogIsLoading = false
                        )
                    }
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _state.update {
                        it.copy(reviewDialogIsLoading = false, reviewDialogError = reportResult.toUserMessage())
                    }
                }
            }
        }
    }

    fun onDismissReviewDialog() {
        _state.update {
            it.copy(
                reviewDialogDocumentId = null,
                reviewDialogReportId = null,
                reviewDialogIsLoading = false,
                reviewDialogError = null
            )
        }
    }

    fun onSaveReview(
        reportId: String,
        observedAt: String?,
        displayTitle: String?,
        displaySubtitle: String?,
        sourceLaboratory: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(reviewDialogError = null) }
            when (val result = medicalResultRepository.updateReport(
                reportId, observedAt, displayTitle, displaySubtitle, sourceLaboratory
            )) {
                is ApiResult.Success -> {
                    globalSyncManager.triggerSync()
                    onDismissReviewDialog()
                }
                is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                    _state.update { it.copy(reviewDialogError = result.toUserMessage()) }
                }
            }
        }
    }

    private suspend fun checkDocumentReadiness(documents: List<PatientDocument>) {
        if (documents.isEmpty()) return
        val readinessBuilder = _state.value.documentReadiness.toMutableMap()
        val displayNamesBuilder = _state.value.documentDisplayNames.toMutableMap()
        val reportCountsBuilder = _state.value.documentReportCounts.toMutableMap()
        val docsToCheck = documents.filter { readinessBuilder[it.id] != DocumentReadiness.Ready }
        if (docsToCheck.isEmpty()) return
        _state.update { it.copy(readinessCheckInProgress = true) }
        docsToCheck.forEach { doc ->
            checkSingleDocument(doc, readinessBuilder, displayNamesBuilder, reportCountsBuilder)
        }
        _state.update {
            it.copy(
                readinessCheckInProgress = false,
                documentReadiness = readinessBuilder.toMap(),
                documentDisplayNames = displayNamesBuilder.toMap(),
                documentReportCounts = reportCountsBuilder.toMap()
            )
        }
    }

    private suspend fun checkSingleDocument(
        doc: PatientDocument,
        readinessBuilder: MutableMap<String, DocumentReadiness>,
        displayNamesBuilder: MutableMap<String, String>,
        reportCountsBuilder: MutableMap<String, Int>
    ) {
        readinessBuilder[doc.id] = DocumentReadiness.Checking
        when (val result = ocrRepository.listExtractions(doc.id)) {
            is ApiResult.Success -> {
                val successRun = result.data.firstOrNull {
                    it.status == OcrStatus.COMPLETED || it.status == OcrStatus.SUCCESS || it.status == OcrStatus.PARTIAL
                }
                if (successRun != null) {
                    val hasReports = successRun.reports.isNotEmpty() || doc.observedAt != null
                    readinessBuilder[doc.id] = if (hasReports) DocumentReadiness.Ready else DocumentReadiness.Unavailable
                    if (hasReports) {
                        loadDisplayNameForDocument(doc, successRun.id, displayNamesBuilder, reportCountsBuilder, successRun.reports.size)
                    }
                } else {
                    val hasFailed = result.data.any { it.status == OcrStatus.FAILED }
                    val hasPending = result.data.any { it.status == OcrStatus.PENDING || it.status == OcrStatus.PROCESSING }
                    readinessBuilder[doc.id] = when {
                        hasFailed -> DocumentReadiness.Failed
                        hasPending -> DocumentReadiness.Pending
                        else -> DocumentReadiness.Unavailable
                    }
                }
            }
            else -> {
                readinessBuilder[doc.id] = DocumentReadiness.Unavailable
            }
        }
    }

    private suspend fun loadDisplayNameForDocument(
        doc: PatientDocument,
        extractionRunId: String,
        displayNamesBuilder: MutableMap<String, String>,
        reportCountsBuilder: MutableMap<String, Int>,
        knownReportCount: Int
    ) {
        val detailResult = ocrRepository.getExtractionStatus(doc.id, extractionRunId)
        if (detailResult !is ApiResult.Success || detailResult.data.reports.isEmpty()) {
            if (knownReportCount > 0) reportCountsBuilder[doc.id] = knownReportCount
            return
        }
        val reports = detailResult.data.reports
        reportCountsBuilder[doc.id] = reports.size
        val reportId = reports.first().id
        when (val reportResult = medicalResultRepository.getReportDetails(reportId)) {
            is ApiResult.Success -> {
                val title = reportResult.data.displayTitle ?: run {
                    val dotIndex = doc.originalFileName.lastIndexOf('.')
                    if (dotIndex > 0) doc.originalFileName.substring(0, dotIndex) else doc.originalFileName
                }
                displayNamesBuilder[doc.id] = title
            }
            else -> { /* keep fallback */ }
        }
    }

    private fun Medication.toUiItem(
        daysRemaining: Int = 0,
        iconType: MedicineIconType = MedicineIconType.PILL
    ): MedicineItem {
        val days = if (effectiveDate != null && endDate != null) {
            maxOf(0L, endDate.toEpochDay() - java.time.LocalDate.now().toEpochDay()).toInt()
        } else {
            daysRemaining
        }
        return MedicineItem(
            id = this.id,
            name = this.name,
            doseValue = this.doseValue,
            doseUnitLabel = this.doseUnitLabel,
            doseUnit = this.doseUnit,
            strength = this.strength,
            frequencyText = this.frequencyText,
            treatmentId = this.treatmentId,
            treatmentName = this.treatmentName,
            schedules = this.schedules.map {
                MedicineScheduleUiItem(
                    administrationTime = it.administrationTime,
                    mealRelationLabel = it.mealRelationLabel,
                    mealRelation = it.mealRelation
                )
            },
            daysRemaining = days,
            iconType = iconType,
            analysisDocumentId = this.analysisDocumentId,
            analysisDocumentIds = this.analysisDocumentIds.ifEmpty { listOfNotNull(this.analysisDocumentId) },
            effectiveDate = this.effectiveDate,
            endDate = this.endDate,
            active = this.active,
            createdAt = this.createdAt,
            attachmentIds = this.attachmentIds,
            ocrReviewConfirmed = this.ocrReviewConfirmed
        )
    }

    private fun PersonalNote.toUiItem(
        severity: NoteSeverity = NoteSeverity.OK
    ): PersonalNoteItem {
        return PersonalNoteItem(
            id = this.id,
            title = this.analysisName,
            content = this.clinicalObservations ?: "",
            author = this.doctorLocation ?: "Eu",
            dateLabel = this.noteDate.toString(),
            severity = severity,
            analysisDocumentId = this.analysisDocumentId,
            analysisDocumentIds = this.analysisDocumentIds.ifEmpty { listOfNotNull(this.analysisDocumentId) },
            noteDate = this.noteDate,
            createdAt = this.createdAt,
            attachmentIds = this.attachmentIds
        )
    }

    private fun newMedicationDraft(): MedicationDraft =
        MedicationDraft(draftId = UUID.randomUUID().toString(), startDate = LocalDate.now())

    private fun MedicationOcrItem.toDraft(startDate: LocalDate): MedicationDraft {
        val commonMeal = schedules.map { it.mealRelation }.distinct().singleOrNull() ?: MealRelation.NO_MEAL_RELATION
        return MedicationDraft(
            draftId = UUID.randomUUID().toString(),
            name = name.orEmpty(),
            doseText = doseValue?.toDoseText().orEmpty(),
            doseUnit = doseUnit?.let { unit -> runCatching { DoseUnit.valueOf(unit) }.getOrNull() },
            strength = strength,
            frequencyText = frequencyText.orEmpty(),
            startDate = startDate,
            durationText = durationDays?.toString().orEmpty(),
            schedules = schedules,
            mealRelation = commonMeal,
            requiresOcrReview = true,
            reviewed = false,
            unconfirmedFields = unconfirmedFields.filter { it != "strength" }
        )
    }

    private fun MedicineItem.toDraft(): MedicationDraft {
        val start = effectiveDate
            ?: createdAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
            ?: LocalDate.now()
        val duration = if (effectiveDate != null && endDate != null) {
            (endDate.toEpochDay() - effectiveDate.toEpochDay() + 1).toInt().takeIf { it > 0 }
        } else null
        val commonMeal = schedules.map { it.mealRelation }.distinct().singleOrNull() ?: MealRelation.NO_MEAL_RELATION
        return MedicationDraft(
            draftId = UUID.randomUUID().toString(),
            id = id,
            name = name,
            doseText = doseValue?.toDoseText().orEmpty(),
            doseUnit = doseUnit,
            strength = strength,
            frequencyText = frequencyText.orEmpty(),
            startDate = start,
            durationText = duration?.toString().orEmpty(),
            schedules = schedules.map { MedicationSchedule(it.administrationTime.take(5), it.mealRelation) },
            mealRelation = commonMeal,
            requiresOcrReview = attachmentIds.isNotEmpty(),
            reviewed = true
        )
    }

    private fun Double.toDoseText(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()

    private fun normalizeIfNeeded(filePath: String): String {
        val file = File(filePath)
        if (!ImageNormalizer.shouldNormalize(file)) return filePath
        return ImageNormalizer.normalize(file)?.absolutePath ?: filePath
    }
}
