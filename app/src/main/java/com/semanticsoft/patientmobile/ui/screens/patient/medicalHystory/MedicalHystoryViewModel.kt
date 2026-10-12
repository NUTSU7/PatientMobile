package com.semanticsoft.patientmobile.ui.screens.patient.medicalHystory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.DoseUnit
import com.semanticsoft.patientmobile.domain.model.MealRelation
import com.semanticsoft.patientmobile.domain.model.Medication
import com.semanticsoft.patientmobile.domain.model.MedicationSchedule
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.PersonalNote
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalHistoryRepository
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.patient.OcrRepository
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.MedicationOcrDraftResponseDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.PersonalNoteOcrDraftResponseDto
import com.semanticsoft.patientmobile.ui.shared.upload.ImageNormalizer
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.ui.screens.patient.medicalHystory.components.ScheduleEntryData
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
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

sealed class MedicalHystoryEvent {
    data class NavigateToReportResults(val reportId: String) : MedicalHystoryEvent()
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
    val doseValue: Double,
    val doseUnitLabel: String,
    val doseUnit: DoseUnit = DoseUnit.CAPSULE,
    val schedules: List<MedicineScheduleUiItem>,
    val daysRemaining: Int,
    val iconType: MedicineIconType = MedicineIconType.PILL,
    val analysisDocumentId: String? = null,
    val effectiveDate: java.time.LocalDate? = null,
    val endDate: java.time.LocalDate? = null,
    val active: Boolean = true,
    val createdAt: java.time.Instant? = null,
    val attachmentIds: List<String> = emptyList(),
    val ocrReviewConfirmed: Boolean = false
)

data class MedicineScheduleUiItem(
    val administrationTime: String,
    val mealRelationLabel: String?,
    val mealRelation: MealRelation = MealRelation.NO_MEAL_RELATION
)

sealed interface MedicationsState {
    data object Loading : MedicationsState
    data class Success(val medications: List<MedicineItem>) : MedicationsState
    data class Error(val message: String) : MedicationsState
}

sealed interface PersonalNotesState {
    data object Loading : PersonalNotesState
    data class Success(val notes: List<PersonalNoteItem>) : PersonalNotesState
    data class Error(val message: String) : PersonalNotesState
}

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
    val noteDate: java.time.LocalDate = java.time.LocalDate.now(),
    val createdAt: java.time.Instant? = null,
    val attachmentIds: List<String> = emptyList()
)

data class MedicalHystoryUiState(
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
    val noteDialogError: String? = null,
    val deleteMedicationTargetId: String? = null,
    val deleteNoteTargetId: String? = null,
    val selectedCombinedSectionTab: CombinedSectionTab = CombinedSectionTab.NOTES,
    val medicationOcrDraftResult: MedicationOcrDraftResponseDto? = null,
    val medicationOcrFeedback: String? = null,
    val medicationOcrFeedbackError: Boolean = false,
    val medicationOcrExtracting: Boolean = false,
    val noteOcrDraftResult: PersonalNoteOcrDraftResponseDto? = null,
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
        get() = deleteMedicationTargetId != null

    val showDeleteNoteDialog: Boolean
        get() = deleteNoteTargetId != null

    val showReviewDialog: Boolean
        get() = reviewDialogDocumentId != null

    val showRetryDialog: Boolean
        get() = retryDialogDocumentId != null

    val readyDocuments: List<PatientDocument>
        get() = allDocuments.filter { documentReadiness[it.id] == DocumentReadiness.Ready }
    val hasPendingDocuments: Boolean
        get() = allDocuments.any { documentReadiness[it.id] in listOf(DocumentReadiness.Checking, DocumentReadiness.Pending) }

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
                if (med.analysisDocumentId == null) return@filter false
                med.analysisDocumentId in docIds
            }
        }
    val filteredNotes: List<PersonalNoteItem>
        get() {
            val docIds = visibleDocumentIds
            return notes.filter { note ->
                if (!isDateFilterActive) return@filter true
                if (note.analysisDocumentId == null) return@filter false
                note.analysisDocumentId in docIds
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

@HiltViewModel
class MedicalHystoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val medicalResultRepository: MedicalResultRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val ocrRepository: OcrRepository,
    private val globalSyncManager: GlobalSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(MedicalHystoryUiState(isLoading = true))
    val state: StateFlow<MedicalHystoryUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    private val _events = MutableSharedFlow<MedicalHystoryEvent>()
    val events: SharedFlow<MedicalHystoryEvent> = _events.asSharedFlow()

    private val _medicationsState = MutableStateFlow<MedicationsState>(MedicationsState.Loading)
    val medicationsState: StateFlow<MedicationsState> = _medicationsState.asStateFlow()

    private val _notesState = MutableStateFlow<PersonalNotesState>(PersonalNotesState.Loading)
    val notesState: StateFlow<PersonalNotesState> = _notesState.asStateFlow()

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
                    _medicationsState.update { MedicationsState.Success(items) }
                    items
                }
                else -> {
                    val msg = result.toUserMessage()
                    _medicationsState.update { MedicationsState.Error(msg) }
                    _state.value.medicines
                }
            }

            val notes = when (val result = medicalHistoryRepository.getNotes()) {
                is ApiResult.Success -> {
                    val items = result.data
                        .sortedByDescending { it.noteDate }
                        .map { it.toUiItem() }
                    _notesState.update { PersonalNotesState.Success(items) }
                    items
                }
                else -> {
                    val msg = result.toUserMessage()
                    _notesState.update { PersonalNotesState.Error(msg) }
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
            _events.emit(MedicalHystoryEvent.NavigateToReportResults(firstReport.id))
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

    fun selectPreviousYear() {
        val years = _state.value.availableYears
        if (years.isEmpty()) return
        val selectedYear = _state.value.selectedYear
        if (selectedYear == null) {
            onYearSelected(years.first())
            return
        }
        val currentIndex = years.indexOf(selectedYear)
        if (currentIndex >= 0 && currentIndex < years.lastIndex) {
            onYearSelected(years[currentIndex + 1])
        }
    }

    fun selectNextYear() {
        val years = _state.value.availableYears
        if (years.isEmpty()) return
        val selectedYear = _state.value.selectedYear
        if (selectedYear == null) return
        val currentIndex = years.indexOf(selectedYear)
        if (currentIndex > 0) {
            onYearSelected(years[currentIndex - 1])
        } else {
            onYearSelected(null)
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
        _state.update { it.copy(medicationDialogTargetId = "add", medicationDialogError = null) }
    }

    fun onShowEditMedicationDialog(medicationId: String) {
        _state.update { it.copy(medicationDialogTargetId = medicationId, medicationDialogError = null) }
    }

    fun onDismissAddMedicationDialog() {
        _state.update { it.copy(medicationDialogTargetId = null, medicationDialogError = null) }
        clearMedicationOcrDraft()
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
                    _state.update {
                        it.copy(
                            medicationOcrDraftResult = draft,
                            medicationOcrExtracting = false,
                            medicationOcrFeedback = "Sugestiile OCR au fost aplicate \u00EEn c\u00E2mpuri.",
                            medicationOcrFeedbackError = false,
                            ocrMedicationAttachmentIds = attachmentIds
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

    fun confirmMedicationOcrReview() {
        // cleared by dialog dismiss
    }

    fun addMedicine(
        name: String,
        doseValue: Double,
        doseUnit: String,
        schedules: List<ScheduleEntryData>,
        durationDays: Int,
        associatedDocumentId: String?
    ) {
        if (name.isBlank()) {
            _state.update { it.copy(medicationDialogError = "Denumirea medicamentului este obligatorie.") }
            return
        }
        if (doseValue <= 0.0) {
            _state.update { it.copy(medicationDialogError = "Doza trebuie s\u0103 fie mai mare dec\u00E2t 0.") }
            return
        }
        val emptySchedules = schedules.isEmpty()
        if (emptySchedules) {
            _state.update { it.copy(medicationDialogError = "Adaug\u0103 cel pu\u021Bin o or\u0103 de administrare.") }
            return
        }
        val targetId = _state.value.medicationDialogTargetId
        val isEditing = targetId != null && targetId != "add"
        viewModelScope.launch {
            val doseUnitEnum = try { DoseUnit.valueOf(doseUnit) } catch (_: Exception) { DoseUnit.CAPSULE }
            val medicationSchedules = schedules.map { entry ->
                MedicationSchedule(
                    administrationTime = entry.administrationTime,
                    mealRelation = try { MealRelation.valueOf(entry.mealRelation) } catch (_: Exception) { MealRelation.NO_MEAL_RELATION }
                )
            }

            val existing = if (isEditing) _state.value.medicines.find { it.id == targetId } else null
            val effective = existing?.effectiveDate ?: LocalDate.now()
            val end = if (durationDays > 0) effective.plusDays(durationDays.toLong()) else null

            val medication = Medication(
                id = if (isEditing) targetId!! else "",
                name = name,
                doseValue = doseValue,
                doseUnit = doseUnitEnum,
                doseUnitLabel = doseUnitEnum.name.lowercase(),
                schedules = medicationSchedules.ifEmpty {
                    listOf(
                        MedicationSchedule(
                            administrationTime = "08:00",
                            mealRelation = MealRelation.NO_MEAL_RELATION
                        )
                    )
                },
                active = existing?.active ?: true,
                effectiveDate = effective,
                endDate = end,
                analysisDocumentId = associatedDocumentId,
                attachmentIds = (existing?.attachmentIds ?: emptyList()) + _state.value.ocrMedicationAttachmentIds,
                ocrReviewConfirmed = associatedDocumentId != null,
                createdAt = existing?.createdAt
            )

            if (isEditing) {
                when (val result = medicalHistoryRepository.updateMedication(targetId!!, medication)) {
                    is ApiResult.Success -> {
                        globalSyncManager.triggerSync()
                        onDismissAddMedicationDialog()
                    }
                    is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                        _state.update { it.copy(medicationDialogError = result.toUserMessage()) }
                    }
                }
            } else {
                when (val result = medicalHistoryRepository.createMedication(medication)) {
                    is ApiResult.Success -> {
                        globalSyncManager.triggerSync()
                        onDismissAddMedicationDialog()
                    }
                    is ApiResult.HttpError, is ApiResult.NetworkError, is ApiResult.AuthError -> {
                        _state.update { it.copy(medicationDialogError = result.toUserMessage()) }
                    }
                }
            }
        }
    }

    fun addNote(
        title: String,
        doctorLocation: String,
        content: String,
        analysisDocumentId: String? = null
    ) {
        if (title.isBlank()) {
            _state.update { it.copy(noteDialogError = "Titlul noti\u021Bei este obligatoriu.") }
            return
        }
        val targetId = _state.value.noteDialogTargetId
        val isEditing = targetId != null && targetId != "add"
        viewModelScope.launch {
            val existing = if (isEditing) _state.value.notes.find { it.id == targetId } else null
            val note = PersonalNote(
                id = if (isEditing) targetId!! else "",
                analysisName = title,
                doctorLocation = doctorLocation,
                clinicalObservations = content,
                noteDate = existing?.noteDate ?: LocalDate.now(),
                analysisDocumentId = analysisDocumentId,
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

    fun attachFile(document: PatientDocument) {
        _state.update { current ->
            val docs = listOf(document) + current.allDocuments
            val years = docs
                .mapNotNull { it.observedAt ?: it.uploadedAt }
                .map { it.atZone(ZoneId.systemDefault()).year }
                .distinct()
                .sortedDescending()
            val currentYear = LocalDate.now().year
            val selectedYear = when {
                current.selectedYear == null -> null
                years.contains(current.selectedYear) -> current.selectedYear
                years.contains(currentYear) -> currentYear
                years.isNotEmpty() -> years.first()
                else -> null
            }
            val updatedState = current.copy(
                allDocuments = docs,
                availableYears = years,
                selectedYear = selectedYear
            )
            updatedState.copy(
                selectedDate = if (selectedYear != null) updatedState.observedDatesForSelectedYear.firstOrNull() else null
            )
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
            noteDate = this.noteDate,
            createdAt = this.createdAt,
            attachmentIds = this.attachmentIds
        )
    }

    private fun normalizeIfNeeded(filePath: String): String {
        val file = File(filePath)
        if (!ImageNormalizer.shouldNormalize(file)) return filePath
        return ImageNormalizer.normalize(file)?.absolutePath ?: filePath
    }
}
