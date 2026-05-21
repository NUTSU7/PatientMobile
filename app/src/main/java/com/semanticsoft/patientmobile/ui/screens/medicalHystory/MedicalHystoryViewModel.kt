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
import com.semanticsoft.patientmobile.domain.repository.MedicalHistoryRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MedicalTimelineMode {
    MONTHS,
    WEEK_DAYS
}

enum class MedicineIconType {
    PILL,
    CAPSULE,
    SPRAY,
    SYRUP,
    INJECTION,
    DROPS
}

data class MedicineItem(
    val id: String,
    val name: String,
    val doseValue: Double,
    val doseUnitLabel: String,
    val schedules: List<MedicineScheduleUiItem>,
    val daysRemaining: Int,
    val iconType: MedicineIconType = MedicineIconType.PILL
)

data class MedicineScheduleUiItem(
    val administrationTime: String,
    val mealRelationLabel: String?
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
    val accentColor: androidx.compose.ui.graphics.Color? = null
)

data class MedicalHystoryUiState(
    val greetingName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allDocuments: List<PatientDocument> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int = LocalDate.now().year,
    val timelineMode: MedicalTimelineMode = MedicalTimelineMode.MONTHS,
    val selectedTimelineValue: Int? = null,
    val medicines: List<MedicineItem> = emptyList(),
    val notes: List<PersonalNoteItem> = emptyList(),
    val selectedDocumentId: String? = null,
    val documentResults: Map<String, List<com.semanticsoft.patientmobile.domain.model.MedicalResult>> = emptyMap()
) {
    val analysisDocuments: List<PatientDocument>
        get() = allDocuments

    val timelineValuesForSelectedYear: List<Int>
        get() = when (timelineMode) {
            MedicalTimelineMode.MONTHS -> {
                analysisDocuments
                    .map { it.uploadedAt.atZone(ZoneId.systemDefault()) }
                    .filter { it.year == selectedYear }
                    .map { it.monthValue }
                    .distinct()
                    .sorted()
            }
            MedicalTimelineMode.WEEK_DAYS -> {
                analysisDocuments
                    .map { it.uploadedAt.atZone(ZoneId.systemDefault()) }
                    .filter { it.year == selectedYear }
                    .map { it.dayOfWeek.value }
                    .distinct()
                    .sorted()
            }
        }

    val filteredDocuments: List<PatientDocument>
        get() {
            return analysisDocuments.filter { document ->
                val zonedDate = document.uploadedAt.atZone(ZoneId.systemDefault())
                if (zonedDate.year != selectedYear) return@filter false
                when (timelineMode) {
                    MedicalTimelineMode.MONTHS -> {
                        selectedTimelineValue == null || zonedDate.monthValue == selectedTimelineValue
                    }
                    MedicalTimelineMode.WEEK_DAYS -> {
                        selectedTimelineValue == null || zonedDate.dayOfWeek.value == selectedTimelineValue
                    }
                }
            }.sortedByDescending { it.uploadedAt }
        }
}

@HiltViewModel
class MedicalHystoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val medicalResultRepository: MedicalResultRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MedicalHystoryUiState(isLoading = true))
    val state: StateFlow<MedicalHystoryUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    private val _medicationsState = MutableStateFlow<MedicationsState>(MedicationsState.Loading)
    val medicationsState: StateFlow<MedicationsState> = _medicationsState.asStateFlow()

    private val _notesState = MutableStateFlow<PersonalNotesState>(PersonalNotesState.Loading)
    val notesState: StateFlow<PersonalNotesState> = _notesState.asStateFlow()

    init {
        refreshUserProfile()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val hasData = _state.value.allDocuments.isNotEmpty()
            if (!hasData) {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
            }

            var errorMessage: String? = null

            val documents = when (val result = documentRepository.getDocuments(0, 50)) {
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

            val years = documents
                .map { it.uploadedAt.atZone(ZoneId.systemDefault()).year }
                .distinct()
                .sortedDescending()
            val currentYear = LocalDate.now().year
            val selectedYear = when {
                years.contains(_state.value.selectedYear) -> _state.value.selectedYear
                years.contains(currentYear) -> currentYear
                years.isNotEmpty() -> years.first()
                else -> currentYear
            }
            val latestMonthInYear = documents
                .filter { it.uploadedAt.atZone(ZoneId.systemDefault()).year == selectedYear }
                .maxByOrNull { it.uploadedAt }
                ?.uploadedAt
                ?.atZone(ZoneId.systemDefault())
                ?.monthValue

            val medicines = when (val result = medicalHistoryRepository.getMedications()) {
                is ApiResult.Success -> {
                    val items = result.data.map { it.toUiItem() }
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
                    val items = result.data.map { it.toUiItem() }
                    _notesState.update { PersonalNotesState.Success(items) }
                    items
                }
                else -> {
                    val msg = result.toUserMessage()
                    _notesState.update { PersonalNotesState.Error(msg) }
                    _state.value.notes
                }
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = errorMessage,
                    allDocuments = documents,
                    availableYears = years,
                    selectedYear = selectedYear,
                    selectedTimelineValue = latestMonthInYear
                        ?: it.timelineValuesForSelectedYear.firstOrNull(),
                    medicines = medicines,
                    notes = notes
                )
            }
        }
    }

    fun onDocumentSelected(documentId: String) {
        _state.update { it.copy(selectedDocumentId = documentId) }
        if (_state.value.documentResults.containsKey(documentId)) return

        viewModelScope.launch {
            when (val result = medicalResultRepository.getByDocumentId(documentId)) {
                is ApiResult.Success -> {
                    _state.update { current ->
                        current.copy(
                            documentResults = current.documentResults + (documentId to result.data)
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun onTimelineModeChange(mode: MedicalTimelineMode) {
        _state.update { current ->
            val updated = current.copy(timelineMode = mode)
            updated.copy(selectedTimelineValue = updated.timelineValuesForSelectedYear.firstOrNull())
        }
    }

    fun onYearSelected(year: Int) {
        _state.update { current ->
            val updated = current.copy(selectedYear = year)
            updated.copy(selectedTimelineValue = updated.timelineValuesForSelectedYear.firstOrNull())
        }
    }

    fun selectPreviousYear() {
        val years = _state.value.availableYears
        if (years.isEmpty()) return
        val currentIndex = years.indexOf(_state.value.selectedYear)
        if (currentIndex >= 0 && currentIndex < years.lastIndex) {
            onYearSelected(years[currentIndex + 1])
        }
    }

    fun selectNextYear() {
        val years = _state.value.availableYears
        if (years.isEmpty()) return
        val currentIndex = years.indexOf(_state.value.selectedYear)
        if (currentIndex > 0) {
            onYearSelected(years[currentIndex - 1])
        }
    }

    fun onTimelineValueSelected(value: Int?) {
        _state.update { it.copy(selectedTimelineValue = value) }
    }

    fun addMedicine(
        name: String,
        dosage: String,
        schedule: String,
        daysRemaining: Int,
        iconType: MedicineIconType = MedicineIconType.PILL
    ) {
        viewModelScope.launch {
            val schedules = schedule.split("·", ",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map {
                    MedicationSchedule(
                        administrationTime = it,
                        mealRelation = MealRelation.NO_MEAL_RELATION
                    )
                }
                .ifEmpty {
                    listOf(
                        MedicationSchedule(
                            administrationTime = schedule,
                            mealRelation = MealRelation.NO_MEAL_RELATION
                        )
                    )
                }

            val medication = Medication(
                id = "",
                name = name,
                doseValue = 1.0,
                doseUnit = DoseUnit.TABLET,
                doseUnitLabel = "tablet",
                schedules = schedules,
                createdAt = null
            )

            when (val result = medicalHistoryRepository.createMedication(medication)) {
                is ApiResult.Success -> {
                    _state.update { current ->
                        current.copy(
                            medicines = listOf(result.data.toUiItem(daysRemaining, iconType))
                                    + current.medicines
                        )
                    }
                    (medicationsState.value as? MedicationsState.Success)?.let { currentState ->
                        _medicationsState.update {
                            MedicationsState.Success(
                                listOf(result.data.toUiItem(daysRemaining, iconType))
                                    + currentState.medications
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun addNote(
        title: String,
        content: String,
        author: String = "Me",
        severity: NoteSeverity = NoteSeverity.OK
    ) {
        viewModelScope.launch {
            val note = PersonalNote(
                id = "",
                analysisName = title,
                doctorLocation = author,
                clinicalObservations = content,
                noteDate = LocalDate.now()
            )
            when (val result = medicalHistoryRepository.createNote(note)) {
                is ApiResult.Success -> {
                    _state.update { current ->
                        current.copy(
                            notes = listOf(result.data.toUiItem(severity)) + current.notes
                        )
                    }
                    (notesState.value as? PersonalNotesState.Success)?.let { currentState ->
                        _notesState.update {
                            PersonalNotesState.Success(
                                listOf(result.data.toUiItem(severity)) + currentState.notes
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun attachFile(document: PatientDocument) {
        _state.update { current ->
            val docs = listOf(document) + current.allDocuments
            val years = docs
                .map { it.uploadedAt.atZone(ZoneId.systemDefault()).year }
                .distinct()
                .sortedDescending()
            val currentYear = LocalDate.now().year
            val selectedYear = when {
                years.contains(current.selectedYear) -> current.selectedYear
                years.contains(currentYear) -> currentYear
                years.isNotEmpty() -> years.first()
                else -> currentYear
            }
            val updatedState = current.copy(
                allDocuments = docs,
                availableYears = years,
                selectedYear = selectedYear
            )
            updatedState.copy(
                selectedTimelineValue = updatedState.timelineValuesForSelectedYear.firstOrNull()
            )
        }
    }

    private fun refreshUserProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is ApiResult.Success -> {
                    val user = result.data
                    val greetingName = user.firstName.ifBlank { user.email.substringBefore("@") }
                    _state.update { it.copy(greetingName = greetingName) }
                }
                else -> {}
            }
        }
    }

    private fun Medication.toUiItem(
        daysRemaining: Int = 0,
        iconType: MedicineIconType = MedicineIconType.PILL
    ): MedicineItem {
        return MedicineItem(
            id = this.id,
            name = this.name,
            doseValue = this.doseValue,
            doseUnitLabel = this.doseUnitLabel,
            schedules = this.schedules.map {
                MedicineScheduleUiItem(
                    administrationTime = it.administrationTime,
                    mealRelationLabel = it.mealRelationLabel
                )
            },
            daysRemaining = daysRemaining,
            iconType = iconType
        )
    }

    private fun PersonalNote.toUiItem(
        severity: NoteSeverity = NoteSeverity.OK
    ): PersonalNoteItem {
        return PersonalNoteItem(
            id = this.id,
            title = this.analysisName,
            content = this.clinicalObservations ?: "",
            author = this.doctorLocation ?: "Me",
            dateLabel = this.noteDate.toString(),
            severity = severity
        )
    }
}

fun timelineLabel(mode: MedicalTimelineMode, value: Int): String {
    return when (mode) {
        MedicalTimelineMode.MONTHS -> monthLabel(value)
        MedicalTimelineMode.WEEK_DAYS -> dayOfWeekLabel(value)
    }
}

fun monthLabel(monthValue: Int): String {
    return when (monthValue) {
        1 -> "Ian"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "Mai"
        6 -> "Iun"
        7 -> "Iul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Noi"
        12 -> "Dec"
        else -> "-"
    }
}

fun dayOfWeekLabel(dayOfWeekValue: Int): String {
    return when (dayOfWeekValue) {
        DayOfWeek.MONDAY.value -> "Luni"
        DayOfWeek.TUESDAY.value -> "Marți"
        DayOfWeek.WEDNESDAY.value -> "Miercuri"
        DayOfWeek.THURSDAY.value -> "Joi"
        DayOfWeek.FRIDAY.value -> "Vineri"
        DayOfWeek.SATURDAY.value -> "Sâmbătă"
        DayOfWeek.SUNDAY.value -> "Duminică"
        else -> "-"
    }
}
