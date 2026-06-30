package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.DoseUnit
import com.semanticsoft.patientmobile.domain.model.MealRelation
import com.semanticsoft.patientmobile.domain.model.Medication
import com.semanticsoft.patientmobile.domain.model.MedicationSchedule
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.PersonalNote
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.MedicalHistoryRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.OcrRepository
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.ui.screens.medicalHystory.components.ScheduleEntryData
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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

enum class DocumentReadiness { Checking, Pending, Ready, Unavailable }

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
    val selectedCombinedSectionTab: CombinedSectionTab = CombinedSectionTab.NOTES
) {
    val showAddMedicationDialog: Boolean
        get() = medicationDialogTargetId != null

    val showAddNoteDialog: Boolean
        get() = noteDialogTargetId != null

    val showDeleteMedicationDialog: Boolean
        get() = deleteMedicationTargetId != null

    val showDeleteNoteDialog: Boolean
        get() = deleteNoteTargetId != null

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

            if (errorMessage != null && hasData) {
                _refreshErrors.emit(errorMessage)
                return@launch
            }

            launch {
                checkDocumentReadiness(documents)
            }

            val years = documents
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
                    allDocuments = documents,
                    availableYears = years,
                    selectedYear = selectedYear,
                    selectedDate = null,
                    medicines = medicines,
                    notes = notes
                )
            }
        }
    }

    fun onDocumentSelected(documentId: String) {
        viewModelScope.launch {
            val extractionsResult = ocrRepository.listExtractions(documentId)
            if (extractionsResult !is ApiResult.Success) {
                _refreshErrors.emit("Nu s-au putut \u00EEnc\u0103rca rezultatele.")
                return@launch
            }
            val successRun = extractionsResult.data.firstOrNull {
                it.status == OcrStatus.COMPLETED || it.status == OcrStatus.SUCCESS
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
    }

    fun onShowAddNoteDialog() {
        _state.update { it.copy(noteDialogTargetId = "add", noteDialogError = null) }
    }

    fun onShowEditNoteDialog(noteId: String) {
        _state.update { it.copy(noteDialogTargetId = noteId, noteDialogError = null) }
    }

    fun onDismissAddNoteDialog() {
        _state.update { it.copy(noteDialogTargetId = null, noteDialogError = null) }
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
                attachmentIds = existing?.attachmentIds ?: emptyList(),
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
                attachmentIds = existing?.attachmentIds ?: emptyList(),
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
        _state.update { it.copy(readinessCheckInProgress = true) }
        coroutineScope {
            val semaphore = Semaphore(8)
            documents.map { doc ->
                async {
                    semaphore.withPermit {
                        checkSingleDocument(doc, readinessBuilder)
                    }
                }
            }.awaitAll()
        }
        _state.update { it.copy(readinessCheckInProgress = false) }
    }

    private suspend fun checkSingleDocument(
        doc: PatientDocument,
        readinessBuilder: MutableMap<String, DocumentReadiness>
    ) {
        readinessBuilder[doc.id] = DocumentReadiness.Checking
        _state.update { it.copy(documentReadiness = readinessBuilder.toMap()) }
        when (val result = ocrRepository.listExtractions(doc.id)) {
            is ApiResult.Success -> {
                val successRun = result.data.firstOrNull {
                    it.status == OcrStatus.COMPLETED || it.status == OcrStatus.SUCCESS
                }
                if (successRun != null) {
                    val hasReports = doc.observedAt != null
                    readinessBuilder[doc.id] = if (hasReports) DocumentReadiness.Ready else DocumentReadiness.Unavailable
                } else {
                    val hasPending = result.data.any { it.status == OcrStatus.PENDING || it.status == OcrStatus.PROCESSING }
                    readinessBuilder[doc.id] = if (hasPending) DocumentReadiness.Pending else DocumentReadiness.Unavailable
                }
            }
            else -> {
                readinessBuilder[doc.id] = DocumentReadiness.Unavailable
            }
        }
        _state.update { it.copy(documentReadiness = readinessBuilder.toMap()) }
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
}
