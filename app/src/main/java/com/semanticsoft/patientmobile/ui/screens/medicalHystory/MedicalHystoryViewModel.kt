package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import com.semanticsoft.patientmobile.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class MedicalTimelineMode {
    MONTHS,
    WEEK_DAYS
}

data class MedicineItem(
    val name: String,
    val schedule: String,
    val daysRemaining: Int
)

enum class NoteSeverity {
    GOOD,
    OK,
    BAD
}

private val DEMO_MEDICINES = emptyList<MedicineItem>()

private val DEMO_NOTES = emptyList<PersonalNoteItem>()

private val DEMO_MEDICINES_POPULATED = listOf(
    MedicineItem(name = "Paracetamol", schedule = "08:00 · 20:00", daysRemaining = 5),
    MedicineItem(name = "Amoxicillin", schedule = "09:00 · 15:00 · 21:00", daysRemaining = 3),
    MedicineItem(name = "Vitamin D", schedule = "08:00", daysRemaining = 30)
)

private val DEMO_NOTES_POPULATED = listOf(
    PersonalNoteItem(
        title = "Follow-up",
        content = "Patient to follow up in two weeks regarding blood pressure.",
        author = "Dr. Demo",
        dateLabel = LocalDate.now().toString(),
        severity = NoteSeverity.OK
    ),
    PersonalNoteItem(
        title = "Allergy note",
        content = "Allergic to penicillin - flag on record.",
        author = "Nurse Demo",
        dateLabel = LocalDate.now().minusDays(3).toString(),
        severity = NoteSeverity.BAD
    )
)

private val DEMO_ANALYSIS_DOCUMENTS = listOf(
    PatientDocument(
        id = "demo-doc-1",
        ownerUserId = "demo-user",
        originalFileName = "Patient 1-1.pdf",
        mimeType = "application/pdf",
        fileSizeBytes = 1_245_338,
        uploadedAt = Instant.parse("2024-07-11T13:23:00Z"),
        syncStatus = com.semanticsoft.patientmobile.domain.model.SyncStatus.SYNCED
    )
)

data class PersonalNoteItem(
    val title: String,
    val content: String,
    val author: String,
    val dateLabel: String,
    val severity: NoteSeverity
)

data class MedicalHystoryUiState(
    val greetingName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allDocuments: List<PatientDocument> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int = 2024,
    val timelineMode: MedicalTimelineMode = MedicalTimelineMode.MONTHS,
    val selectedTimelineValue: Int? = null,
    val medicines: List<MedicineItem> = DEMO_MEDICINES,
    val notes: List<PersonalNoteItem> = DEMO_NOTES
) {
    val analysisDocuments: List<PatientDocument>
        get() = when {
            allDocuments.isNotEmpty() -> allDocuments
            BuildConfig.DEBUG -> DEMO_ANALYSIS_DOCUMENTS
            else -> emptyList()
        }

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
                if (zonedDate.year != selectedYear) {
                    return@filter false
                }

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
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(MedicalHystoryUiState(isLoading = true))
    val stateFlow: StateFlow<MedicalHystoryUiState> = _stateFlow.asStateFlow()

    init {
        refreshUserProfile()
        refreshDocuments()
        if (BuildConfig.DEBUG) {
            _stateFlow.value = _stateFlow.value.copy(
                medicines = DEMO_MEDICINES_POPULATED,
                notes = DEMO_NOTES_POPULATED
            )
        }
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
                    val data = if (result.data.isNotEmpty() || !BuildConfig.DEBUG) {
                        result.data
                    } else {
                        DEMO_ANALYSIS_DOCUMENTS
                    }
                    val years = data
                        .map { it.uploadedAt.atZone(ZoneId.systemDefault()).year }
                        .distinct()
                        .sortedDescending()
                    val currentYear = java.time.LocalDate.now().year
                    val selectedYear = when {
                        years.contains(_stateFlow.value.selectedYear) -> _stateFlow.value.selectedYear
                        years.contains(currentYear) -> currentYear
                        years.isNotEmpty() -> years.first()
                        else -> currentYear
                    }

                    val updatedState = _stateFlow.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        allDocuments = data,
                        availableYears = years,
                        selectedYear = selectedYear
                    )

                    _stateFlow.value = updatedState.copy(
                        selectedTimelineValue = updatedState.timelineValuesForSelectedYear.firstOrNull()
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onTimelineModeChange(mode: MedicalTimelineMode) {
        val updated = _stateFlow.value.copy(timelineMode = mode)
        _stateFlow.value = updated.copy(
            selectedTimelineValue = updated.timelineValuesForSelectedYear.firstOrNull()
        )
    }

    fun onYearSelected(year: Int) {
        val updated = _stateFlow.value.copy(selectedYear = year)
        _stateFlow.value = updated.copy(
            selectedTimelineValue = updated.timelineValuesForSelectedYear.firstOrNull()
        )
    }

    fun selectPreviousYear() {
        val years = _stateFlow.value.availableYears
        if (years.isEmpty()) return

        val currentIndex = years.indexOf(_stateFlow.value.selectedYear)
        if (currentIndex >= 0 && currentIndex < years.lastIndex) {
            onYearSelected(years[currentIndex + 1])
        }
    }

    fun selectNextYear() {
        val years = _stateFlow.value.availableYears
        if (years.isEmpty()) return

        val currentIndex = years.indexOf(_stateFlow.value.selectedYear)
        if (currentIndex > 0) {
            onYearSelected(years[currentIndex - 1])
        }
    }

    fun onTimelineValueSelected(value: Int?) {
        _stateFlow.value = _stateFlow.value.copy(selectedTimelineValue = value)
    }

    fun addMedicine(name: String, schedule: String, daysRemaining: Int) {
        val current = _stateFlow.value.medicines.toMutableList()
        current.add(0, MedicineItem(name = name, schedule = schedule, daysRemaining = daysRemaining))
        _stateFlow.value = _stateFlow.value.copy(medicines = current)
    }

    fun addNote(title: String, content: String, author: String = "Me", severity: NoteSeverity = NoteSeverity.OK) {
        val note = PersonalNoteItem(
            title = title,
            content = content,
            author = author,
            dateLabel = LocalDate.now().toString(),
            severity = severity
        )
        val current = _stateFlow.value.notes.toMutableList()
        current.add(0, note)
        _stateFlow.value = _stateFlow.value.copy(notes = current)
    }

    fun attachFile(document: PatientDocument) {
        val current = listOf(document) + _stateFlow.value.allDocuments
        val years = current
            .map { it.uploadedAt.atZone(ZoneId.systemDefault()).year }
            .distinct()
            .sortedDescending()
        val currentYear = java.time.LocalDate.now().year
        val selectedYear = when {
            years.contains(_stateFlow.value.selectedYear) -> _stateFlow.value.selectedYear
            years.contains(currentYear) -> currentYear
            years.isNotEmpty() -> years.first()
            else -> currentYear
        }

        val updatedState = _stateFlow.value.copy(
            allDocuments = current,
            availableYears = years,
            selectedYear = selectedYear
        )
        _stateFlow.value = updatedState.copy(
            selectedTimelineValue = updatedState.timelineValuesForSelectedYear.firstOrNull()
        )
    }

    private fun refreshUserProfile() {
        viewModelScope.launch {
            runCatching { authRepository.getCurrentUser() }
                .onSuccess { user ->
                    val greetingName = user.firstName.ifBlank { user.email.substringBefore("@") }
                    _stateFlow.value = _stateFlow.value.copy(greetingName = greetingName)
                }
        }
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
