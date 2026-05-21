package com.semanticsoft.patientmobile.ui.screens.medicalResults

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicalResultItem(
    val id: String,
    val originalTestName: String,
    val canonicalName: String,
    val valueNumeric: Double?,
    val valueText: String?,
    val unit: String,
    val referenceLow: Double?,
    val referenceHigh: Double?,
    val referenceText: String?,
    val abnormalFlag: String?,
    val observedAt: String?
)

sealed interface ResultsState {
    data object Loading : ResultsState
    data class Success(val groupedResults: Map<String, List<MedicalResultItem>>) : ResultsState
    data class Error(val message: String) : ResultsState
}

sealed interface AiSummaryState {
    data object Pending : AiSummaryState
    data class Ready(val summary: String) : AiSummaryState
    data class Error(val message: String) : AiSummaryState
}

@HiltViewModel
class MedicalResultsViewModel @Inject constructor(
    private val medicalResultRepository: MedicalResultRepository
) : ViewModel() {

    private val _resultsState = MutableStateFlow<ResultsState>(ResultsState.Loading)
    val resultsState: StateFlow<ResultsState> = _resultsState.asStateFlow()

    private val _aiSummaryState = MutableStateFlow<AiSummaryState>(AiSummaryState.Pending)
    val aiSummaryState: StateFlow<AiSummaryState> = _aiSummaryState.asStateFlow()

    init {
        loadResults()
        loadAiSummary()
    }

    fun loadResults() {
        viewModelScope.launch {
            _resultsState.update { ResultsState.Loading }

            when (val result = medicalResultRepository.getAllResults()) {
                is ApiResult.Success -> {
                    val grouped = result.data
                        .map { it.toUiItem() }
                        .groupBy { item ->
                            result.data.firstOrNull { it.id == item.id }?.analysisGroup
                                ?: "Altele"
                        }
                    _resultsState.update { ResultsState.Success(grouped) }
                }
                else -> {
                    _resultsState.update { ResultsState.Error(result.toUserMessage()) }
                }
            }
        }
    }

    fun loadResultsByGroup(analysisGroup: String) {
        viewModelScope.launch {
            _resultsState.update { ResultsState.Loading }

            when (val result = medicalResultRepository.getAllResults(
                analysisGroup = analysisGroup
            )) {
                is ApiResult.Success -> {
                    val grouped = result.data
                        .map { it.toUiItem() }
                        .groupBy { item ->
                            result.data.firstOrNull { it.id == item.id }?.analysisGroup
                                ?: "Altele"
                        }
                    _resultsState.update { ResultsState.Success(grouped) }
                }
                else -> {
                    _resultsState.update { ResultsState.Error(result.toUserMessage()) }
                }
            }
        }
    }

    fun loadAiSummary() {
        viewModelScope.launch {
            _aiSummaryState.update { AiSummaryState.Pending }

            when (val result = medicalResultRepository.getAiSummary()) {
                is ApiResult.Success -> {
                    val response = result.data
                    _aiSummaryState.update {
                        when (response.status.uppercase()) {
                            "READY", "COMPLETED" ->
                                AiSummaryState.Ready(response.summaryText)
                            else -> AiSummaryState.Pending
                        }
                    }
                }
                else -> {
                    _aiSummaryState.update { AiSummaryState.Error(result.toUserMessage()) }
                }
            }
        }
    }

    fun loadHistoryForResult(testDefinitionId: String) {
        viewModelScope.launch {
            when (val result = medicalResultRepository.getResultsHistory(testDefinitionId)) {
                is ApiResult.Success -> { }
                else -> { }
            }
        }
    }

    private fun MedicalResult.toUiItem(): MedicalResultItem = MedicalResultItem(
        id = id,
        originalTestName = originalTestName,
        canonicalName = canonicalName,
        valueNumeric = valueNumeric,
        valueText = valueText,
        unit = unit,
        referenceLow = referenceLow,
        referenceHigh = referenceHigh,
        referenceText = referenceText,
        abnormalFlag = abnormalFlag,
        observedAt = observedAt?.toString()
    )
}
