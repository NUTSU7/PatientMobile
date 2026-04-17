package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.data.model.AttentionItem
import com.semanticsoft.patientmobile.data.model.BasicIndicatorItem
import com.semanticsoft.patientmobile.data.model.ClinicalPillarCardItem
import com.semanticsoft.patientmobile.data.model.GeneralMarkerCardItem
import com.semanticsoft.patientmobile.data.model.MarkerCategoryItem
import com.semanticsoft.patientmobile.data.model.MarkerSummary
import com.semanticsoft.patientmobile.data.model.WarningCardItem
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val greetingName: String = "",
    val fullName: String = "",
    val role: String = "",
    val profilePhotoResId: Int? = null,
    val hasUploadedDocuments: Boolean = true,
    val lastAnalysisDate: String = "15 Mar 2026",
    val attentionItems: List<AttentionItem> = emptyList(),
    val basicIndicators: List<BasicIndicatorItem> = emptyList(),
    val markerCategories: List<MarkerCategoryItem> = emptyList(),
    val generalMarkerCards: List<GeneralMarkerCardItem> = emptyList(),
    val markerSummary: MarkerSummary = MarkerSummary(0, 0, 0, 0),
    val aiSummary: String = "",
    val warningCards: List<WarningCardItem> = emptyList(),
    val clinicalPillarCards: List<ClinicalPillarCardItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class DashboardEvent {
    data object RefreshCompleted : DashboardEvent()
    data class RefreshFailed(val message: String) : DashboardEvent()
    data object LogoutSuccess : DashboardEvent()
    data class LogoutFailure(val message: String) : DashboardEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val medicalResultRepository: MedicalResultRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val demoAttentionItems = listOf(
        AttentionItem("TSH", "0.3", "mIU/L", "Atenție"),
        AttentionItem("Vitamina D", "18", "ng/mL", "Atenție"),
        AttentionItem("Colesterol LDL", "4.5", "mmol/L", "Atenție")
    )


    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    var state by mutableStateOf(
        DashboardUiState(
            greetingName = savedStateHandle[KEY_GREETING_NAME] ?: "",
            fullName = savedStateHandle[KEY_FULL_NAME] ?: "",
            role = savedStateHandle[KEY_ROLE] ?: "",
            attentionItems = demoAttentionItems,
            markerSummary = MarkerSummary(
                normal = savedStateHandle[KEY_SUMMARY_NORMAL] ?: 0,
                borderline = savedStateHandle[KEY_SUMMARY_BORDERLINE] ?: 0,
                attention = savedStateHandle[KEY_SUMMARY_ATTENTION] ?: 0,
                score = savedStateHandle[KEY_SUMMARY_SCORE] ?: 0
            ),
            aiSummary = savedStateHandle[KEY_AI_SUMMARY] ?: "",
            lastAnalysisDate = savedStateHandle[KEY_LAST_ANALYSIS_DATE] ?: "15 Mar 2026",
            errorMessage = savedStateHandle[KEY_ERROR_MESSAGE]
        )
    )
        private set

    init {
        refreshUserProfile()
        refresh()
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { authRepository.logout() }
                .onSuccess { _events.emit(DashboardEvent.LogoutSuccess) }
                .onFailure {
                    val message = it.message ?: "Deconectarea a eșuat."
                    _events.emit(DashboardEvent.LogoutFailure(message))
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            when (val docsResource = documentRepository.getDocuments().first { it !is Resource.Loading }) {
                is Resource.Success -> {
                    val docs = docsResource.data
                    val allResults = docs.flatMap { document ->
                        when (val resultsResource = medicalResultRepository.getByDocumentId(document.id).first { it !is Resource.Loading }) {
                            is Resource.Success -> resultsResource.data
                            is Resource.Error -> emptyList()
                            Resource.Loading -> emptyList()
                        }
                    }

                    val summary = buildComputedSummary(allResults)
                    val lastDate = docs.maxByOrNull { it.uploadedAt }?.uploadedAt?.toString()?.substringBefore("T")
                        ?: state.lastAnalysisDate

                    savedStateHandle[KEY_LAST_ANALYSIS_DATE] = lastDate
                    savedStateHandle[KEY_SUMMARY_NORMAL] = summary.normal
                    savedStateHandle[KEY_SUMMARY_BORDERLINE] = summary.borderline
                    savedStateHandle[KEY_SUMMARY_ATTENTION] = summary.attention
                    savedStateHandle[KEY_SUMMARY_SCORE] = summary.score

                    val aiSummary = buildAiSummary(docs, summary)
                    savedStateHandle[KEY_AI_SUMMARY] = aiSummary
                    savedStateHandle[KEY_ERROR_MESSAGE] = null

                    state = state.copy(
                        hasUploadedDocuments = docs.isNotEmpty(),
                        markerSummary = summary,
                        attentionItems = state.attentionItems.ifEmpty { demoAttentionItems },
                        aiSummary = aiSummary,
                        lastAnalysisDate = lastDate,
                        isLoading = false,
                        errorMessage = null
                    )
                    _events.emit(DashboardEvent.RefreshCompleted)
                }

                is Resource.Error -> {
                    savedStateHandle[KEY_ERROR_MESSAGE] = docsResource.message
                    state = state.copy(
                        isLoading = false,
                        errorMessage = docsResource.message
                    )
                    _events.emit(DashboardEvent.RefreshFailed(docsResource.message))
                }

                Resource.Loading -> Unit
            }
        }
    }

    private fun refreshUserProfile() {
        viewModelScope.launch {
            runCatching { authRepository.getCurrentUser() }
                .onSuccess { user ->
                    val fullName = "${user.firstName} ${user.lastName}".trim()
                    val greetingName = user.firstName.ifBlank { user.email.substringBefore("@") }
                    val role = "Pacient"

                    savedStateHandle[KEY_GREETING_NAME] = greetingName
                    savedStateHandle[KEY_FULL_NAME] = fullName
                    savedStateHandle[KEY_ROLE] = role

                    state = state.copy(
                        greetingName = greetingName,
                        fullName = fullName,
                        role = role
                    )
                }
        }
    }

    private fun buildComputedSummary(results: List<MedicalResult>): MarkerSummary {
        if (results.isEmpty()) return MarkerSummary(0, 0, 0, 0)

        var normal = 0
        var borderline = 0
        var attention = 0

        results.forEach { result ->
            val value = result.value.toDoubleOrNull()
            val range = parseReferenceRange(result.referenceRange)

            if (value == null || range == null) {
                borderline += 1
            } else {
                val (min, max) = range
                when {
                    value < min || value > max -> attention += 1
                    value <= min * 1.05 || value >= max * 0.95 -> borderline += 1
                    else -> normal += 1
                }
            }
        }

        val total = normal + borderline + attention
        val score = if (total == 0) 0 else ((normal.toFloat() / total.toFloat()) * 100f).toInt()
        return MarkerSummary(normal = normal, borderline = borderline, attention = attention, score = score)
    }

    private fun parseReferenceRange(referenceRange: String): Pair<Double, Double>? {
        val cleaned = referenceRange.replace(" ", "")
        val separator = when {
            cleaned.contains("-") -> "-"
            cleaned.contains("to", ignoreCase = true) -> "to"
            else -> null
        } ?: return null

        val parts = cleaned.split(separator)
        if (parts.size != 2) return null

        val min = parts[0].toDoubleOrNull() ?: return null
        val max = parts[1].toDoubleOrNull() ?: return null
        return min to max
    }

    private fun buildAiSummary(documents: List<PatientDocument>, summary: MarkerSummary): String {
        return if (documents.isEmpty()) {
            "Nu există încă documente încărcate."
        } else {
            "Ai ${documents.size} documente. ${summary.attention} rezultate necesită atenție, ${summary.normal} sunt în limite normale."
        }
    }

    companion object {
        private const val KEY_LAST_ANALYSIS_DATE = "dashboard_last_analysis_date"
        private const val KEY_GREETING_NAME = "dashboard_greeting_name"
        private const val KEY_FULL_NAME = "dashboard_full_name"
        private const val KEY_ROLE = "dashboard_role"
        private const val KEY_SUMMARY_NORMAL = "dashboard_summary_normal"
        private const val KEY_SUMMARY_BORDERLINE = "dashboard_summary_borderline"
        private const val KEY_SUMMARY_ATTENTION = "dashboard_summary_attention"
        private const val KEY_SUMMARY_SCORE = "dashboard_summary_score"
        private const val KEY_AI_SUMMARY = "dashboard_ai_summary"
        private const val KEY_ERROR_MESSAGE = "dashboard_error_message"
    }
}
