package com.semanticsoft.patientmobile.ui.screens.reportResults

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ResultItem(
    val id: String,
    val testName: String,
    val value: String,
    val unit: String,
    val reference: String,
    val abnormalFlag: String?
)

@Immutable
data class ResultGroup(
    val groupName: String,
    val results: List<ResultItem>
)

@Immutable
data class ReportResultsUiState(
    val isLoading: Boolean = true,
    val reportTitle: String = "",
    val observedAtLabel: String = "",
    val clinicalType: String? = null,
    val summary: String? = null,
    val requiresReview: Boolean = false,
    val groupedResults: List<ResultGroup> = emptyList(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val isNonLabReport: Boolean = false
)

@HiltViewModel
class ReportResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicalResultRepository: MedicalResultRepository
) : ViewModel() {

    private val reportId: String = savedStateHandle["reportId"] ?: ""

    private val _state = MutableStateFlow(ReportResultsUiState())
    val state: StateFlow<ReportResultsUiState> = _state.asStateFlow()

    init {
        loadResults()
    }

    fun loadResults() {
        if (reportId.isBlank()) {
            _state.update { it.copy(isLoading = false, errorMessage = "ID raport lips\u0103.") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = medicalResultRepository.getReportDetails(reportId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    val formattedDate = data.observedAt?.let { formatObservedAt(it) } ?: ""

                    val isNonLab = data.clinicalType != null &&
                        data.clinicalType != "LAB_RESULTS" &&
                        data.clinicalType != "LAB_RESULT"

                    val grouped = if (!isNonLab) {
                        groupAndSortResults(data.results)
                    } else {
                        emptyList()
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            reportTitle = data.clinicalType ?: grouped.firstOrNull()?.groupName ?: "",
                            observedAtLabel = formattedDate,
                            clinicalType = data.clinicalType,
                            summary = data.summary,
                            requiresReview = data.requiresReview,
                            groupedResults = grouped,
                            isEmpty = !isNonLab && grouped.isEmpty(),
                            isNonLabReport = isNonLab
                        )
                    }
                }
                is ApiResult.HttpError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (result.code == 404) "Acest raport nu mai este disponibil." else result.toUserMessage()
                        )
                    }
                }
                is ApiResult.NetworkError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.toUserMessage()) }
                }
                is ApiResult.AuthError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.toUserMessage()) }
                }
            }
        }
    }

    private fun groupAndSortResults(results: List<MedicalResult>): List<ResultGroup> {
        return results
            .groupBy { it.analysisGroup.ifEmpty { "Altele" } }
            .map { (groupName, items) ->
                ResultGroup(
                    groupName = groupName,
                    results = items
                        .sortedBy { it.originalTestName }
                        .map { it.toResultItem() }
                )
            }
    }

    private fun MedicalResult.toResultItem(): ResultItem {
        val valueStr = when {
            valueNumeric != null -> valueNumeric.toString()
            !valueText.isNullOrBlank() -> valueText
            else -> "-"
        }
        val referenceStr = buildString {
            when {
                referenceLow != null && referenceHigh != null -> {
                    append(referenceLow.toString())
                    append(" – ")
                    append(referenceHigh.toString())
                    if (unit.isNotBlank()) {
                        append(" ")
                        append(unit)
                    }
                }
                !referenceText.isNullOrBlank() -> append(referenceText)
                else -> append("-")
            }
        }
        return ResultItem(
            id = id,
            testName = originalTestName,
            value = valueStr,
            unit = unit,
            reference = referenceStr,
            abnormalFlag = abnormalFlag
        )
    }

    private fun formatObservedAt(date: LocalDate): String {
        val monthName = when (date.monthValue) {
            1 -> "ianuarie"
            2 -> "februarie"
            3 -> "martie"
            4 -> "aprilie"
            5 -> "mai"
            6 -> "iunie"
            7 -> "iulie"
            8 -> "august"
            9 -> "septembrie"
            10 -> "octombrie"
            11 -> "noiembrie"
            12 -> "decembrie"
            else -> ""
        }
        return "${date.dayOfMonth} $monthName ${date.year}"
    }
}
