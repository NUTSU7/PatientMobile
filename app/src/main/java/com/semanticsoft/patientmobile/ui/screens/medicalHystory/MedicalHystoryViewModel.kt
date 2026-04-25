package com.semanticsoft.patientmobile.ui.screens.medicalHystory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
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

private val DEMO_MEDICINES = listOf(
    MedicineItem(
        name = "Augmentin 1000mg",
        schedule = "1 tabletă · De 2 ori pe zi (08:00, 20:00)",
        daysRemaining = 3
    ),
    MedicineItem(
        name = "Magnerot 500mg",
        schedule = "1 tabletă · O dată pe zi (seara)",
        daysRemaining = 15
    ),
    MedicineItem(
        name = "Vitamina D3 2000 UI",
        schedule = "1 capsulă · Dimineața după masă",
        daysRemaining = 30
    )
)

private val DEMO_NOTES = listOf(
    PersonalNoteItem(
        title = "ANALIZĂ SÂNGE (12 MAI)",
        content = "Valorile indică o ușoară carență de fier și magneziu. Se recomandă suplimentarea dietei și reevaluare peste 3 luni.",
        author = "Dr. Andrei Popescu",
        dateLabel = "16 martie 2026",
        severity = NoteSeverity.BAD
    ),
    PersonalNoteItem(
        title = "ECOGRAFIE ABDOMINALĂ",
        content = "Structură hepatică normală. Fără modificări patologice vizibile. Pacientul va continua monitorizarea anuală standard.",
        author = "Dr. Marcel Iureș",
        dateLabel = "14 martie 2026",
        severity = NoteSeverity.GOOD
    ),
    PersonalNoteItem(
        title = "RECOMANDARE GENERALĂ",
        content = "Monitorizarea tensiunii arteriale de două ori pe zi pe durata tratamentului cu Augmentin.",
        author = "Clinica Sanitar",
        dateLabel = "20 mai 2026",
        severity = NoteSeverity.OK
    )
)

private val DEMO_ANALYSIS_DOCUMENTS = listOf(
    PatientDocument(
        id = "demo-doc-1",
        ownerUserId = "demo-user",
        originalFileName = "Analize sânge (profil complet).pdf",
        mimeType = "application/pdf",
        fileSizeBytes = 1_245_338,
        uploadedAt = Instant.parse("2026-04-12T09:30:00Z"),
        syncStatus = com.semanticsoft.patientmobile.domain.model.SyncStatus.SYNCED
    ),
    PatientDocument(
        id = "demo-doc-2",
        ownerUserId = "demo-user",
        originalFileName = "Panel hormonal complet.pdf",
        mimeType = "application/pdf",
        fileSizeBytes = 937_128,
        uploadedAt = Instant.parse("2026-04-09T08:15:00Z"),
        syncStatus = com.semanticsoft.patientmobile.domain.model.SyncStatus.SYNCED
    ),
    PatientDocument(
        id = "demo-doc-3",
        ownerUserId = "demo-user",
        originalFileName = "Ecografie abdominală.png",
        mimeType = "image/png",
        fileSizeBytes = 523_014,
        uploadedAt = Instant.parse("2026-04-02T11:20:00Z"),
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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allDocuments: List<PatientDocument> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int = java.time.LocalDate.now().year,
    val timelineMode: MedicalTimelineMode = MedicalTimelineMode.MONTHS,
    val selectedTimelineValue: Int? = null,
    val medicines: List<MedicineItem> = DEMO_MEDICINES,
    val notes: List<PersonalNoteItem> = DEMO_NOTES
) {
    val analysisDocuments: List<PatientDocument>
        get() = if (allDocuments.isEmpty()) DEMO_ANALYSIS_DOCUMENTS else allDocuments

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
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _stateFlow = MutableStateFlow(MedicalHystoryUiState(isLoading = true))
    val stateFlow: StateFlow<MedicalHystoryUiState> = _stateFlow.asStateFlow()

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
                    val years = result.data
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
                        allDocuments = result.data,
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
