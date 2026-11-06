package com.semanticsoft.patientmobile.feature.patient.medicalHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.feature.patient.uploadedAnalyses.PatientDocument
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
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
    TREATMENTS,
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
    val schedules: List<MedicineScheduleUiItem> = emptyList(),
    val daysRemaining: Int = 0,
    val iconType: MedicineIconType = MedicineIconType.PILL,
    val analysisDocumentId: String? = null,
    val analysisDocumentIds: List<String> = emptyList(),
    val effectiveDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val active: Boolean = true,
    val createdAt: java.time.Instant? = null,
    val attachmentIds: List<String> = emptyList(),
    val ocrReviewConfirmed: Boolean = false
)

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
    val severity: NoteSeverity = NoteSeverity.OK,
    val accentColor: androidx.compose.ui.graphics.Color? = null,
    val analysisDocumentId: String? = null,
    val analysisDocumentIds: List<String> = emptyList(),
    val noteDate: LocalDate = LocalDate.now(),
    val createdAt: java.time.Instant? = null,
    val attachmentIds: List<String> = emptyList(),
    val analysisSummary: String = ""
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
    val medicationOcrFeedback: String? = null,
    val medicationOcrFeedbackError: Boolean = false,
    val medicationOcrExtracting: Boolean = false,
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
    val reviewDialogObservedAt: LocalDate? = null,
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

    val isEditingMedicationDialog: Boolean
        get() = medicationDialogTargetId != null && medicationDialogTargetId != ADD_TARGET_ID

    val isEditingStandaloneMedication: Boolean
        get() = isEditingMedicationDialog && medicationDialogTreatmentId == null

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

class MedicalHistoryViewModel : ViewModel() {

    private val _state = MutableStateFlow(MedicalHistoryUiState())
    val state: StateFlow<MedicalHistoryUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    private val _events = MutableSharedFlow<MedicalHistoryEvent>()
    val events: SharedFlow<MedicalHistoryEvent> = _events.asSharedFlow()

    fun refresh() {
        // Default no-op
    }

    fun onDocumentSelected(documentId: String) {
        viewModelScope.launch {
            _events.emit(MedicalHistoryEvent.NavigateToReportResults(documentId))
        }
    }

    fun onDismissRetryDialog() {
        _state.update { it.copy(retryDialogDocumentId = null) }
    }

    fun onRetryExtraction() {
        _state.update { it.copy(retryDialogDocumentId = null) }
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
    }

    fun onTreatmentNameChange(name: String) {
        _state.update { it.copy(treatmentName = name) }
    }

    fun onAnalysisDocumentsChange(ids: Set<String>) {
        _state.update { current ->
            current.copy(
                medicationAnalysisDocumentIds = ids.filter { it.isNotEmpty() }.toSet()
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
    }

    fun extractMedicationFromFile(filePath: String) {
        // Local no-op
    }

    fun extractPersonalNoteFromFile(filePath: String) {
        // Local no-op
    }

    fun saveTreatment() {
        onDismissAddMedicationDialog()
    }

    fun addNote(
        title: String,
        doctorLocation: String,
        content: String,
        analysisDocumentIds: List<String> = emptyList()
    ) {
        onDismissAddNoteDialog()
    }

    fun onShowDeleteMedicationDialog(medicationId: String) {
        _state.update { it.copy(deleteMedicationTargetId = medicationId) }
    }

    fun onDismissDeleteMedicationDialog() {
        _state.update { it.copy(deleteMedicationTargetId = null) }
    }

    fun onDeleteMedication(medicationId: String) {
        _state.update { current ->
            current.copy(
                medicines = current.medicines.filterNot { it.id == medicationId },
                deleteMedicationTargetId = null
            )
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
        _state.update { current ->
            current.copy(
                medicines = current.medicines.filterNot { it.treatmentId == treatmentId },
                deleteTreatmentTargetId = null
            )
        }
    }

    fun onShowDeleteNoteDialog(noteId: String) {
        _state.update { it.copy(deleteNoteTargetId = noteId) }
    }

    fun onDismissDeleteNoteDialog() {
        _state.update { it.copy(deleteNoteTargetId = null) }
    }

    fun onDeleteNote(noteId: String) {
        _state.update { current ->
            current.copy(
                notes = current.notes.filterNot { it.id == noteId },
                deleteNoteTargetId = null
            )
        }
    }

    fun onReviewClick(documentId: String) {
        _state.update {
            it.copy(reviewDialogDocumentId = documentId)
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
        onDismissReviewDialog()
    }

    private fun newMedicationDraft(): MedicationDraft =
        MedicationDraft(draftId = UUID.randomUUID().toString(), startDate = LocalDate.now())

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
            doseText = doseValue?.toString().orEmpty(),
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
}
