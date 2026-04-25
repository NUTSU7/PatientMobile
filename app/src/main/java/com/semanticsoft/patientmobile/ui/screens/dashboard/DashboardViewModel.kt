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

    private val demoBasicIndicators = listOf(
        BasicIndicatorItem(
            title = "Hemoglobină",
            value = "13.4",
            unit = "g/dL",
            status = com.semanticsoft.patientmobile.data.model.IndicatorStatus.NORMAL,
            trendDirection = com.semanticsoft.patientmobile.data.model.IndicatorTrendDirection.STABLE,
            trendDelta = "0.0",
            trendDescription = "stabil față de analiza anterioară",
            markerPosition = 0.55f
        ),
        BasicIndicatorItem(
            title = "Vitamina D",
            value = "18",
            unit = "ng/mL",
            status = com.semanticsoft.patientmobile.data.model.IndicatorStatus.ATTENTION,
            trendDirection = com.semanticsoft.patientmobile.data.model.IndicatorTrendDirection.DOWN,
            trendDelta = "-4",
            trendDescription = "în scădere în ultimele 30 zile",
            markerPosition = 0.84f
        )
    )

    private val demoMarkerCategories = listOf(
        MarkerCategoryItem(name = "Toate", count = 8),
        MarkerCategoryItem(name = "Hormonali", count = 2),
        MarkerCategoryItem(name = "Metabolism", count = 3),
        MarkerCategoryItem(name = "Hematologie", count = 3)
    )

    private val demoGeneralMarkerCards = listOf(
        GeneralMarkerCardItem(
            title = "TSH",
            category = "Hormonali",
            value = "0.3",
            unit = "mIU/L",
            status = com.semanticsoft.patientmobile.data.model.IndicatorStatus.BORDERLINE,
            normalRange = "0.4 - 4.0",
            borderlineRange = "0.3 - 0.39",
            attentionRange = "< 0.3"
        ),
        GeneralMarkerCardItem(
            title = "LDL Colesterol",
            category = "Metabolism",
            value = "4.5",
            unit = "mmol/L",
            status = com.semanticsoft.patientmobile.data.model.IndicatorStatus.ATTENTION,
            normalRange = "< 3.0",
            borderlineRange = "3.0 - 3.9",
            attentionRange = ">= 4.0"
        ),
        GeneralMarkerCardItem(
            title = "Leucocite",
            category = "Hematologie",
            value = "6.2",
            unit = "10^9/L",
            status = com.semanticsoft.patientmobile.data.model.IndicatorStatus.NORMAL,
            normalRange = "4.0 - 10.0",
            borderlineRange = "3.5 - 3.9",
            attentionRange = "< 3.5"
        )
    )

    private val demoWarningCards = listOf(
        WarningCardItem(
            level = com.semanticsoft.patientmobile.data.model.WarningLevel.HIGH,
            indicators = listOf(
                com.semanticsoft.patientmobile.data.model.WarningIndicatorItem("Vitamina D", "18", "ng/mL"),
                com.semanticsoft.patientmobile.data.model.WarningIndicatorItem("LDL", "4.5", "mmol/L")
            )
        )
    )

    private val demoClinicalPillarCards = listOf(
        ClinicalPillarCardItem(
            type = com.semanticsoft.patientmobile.data.model.ClinicalPillarType.HEART_CV,
            reportCount = 2
        ),
        ClinicalPillarCardItem(
            type = com.semanticsoft.patientmobile.data.model.ClinicalPillarType.HORMONES,
            reportCount = 2
        ),
        ClinicalPillarCardItem(
            type = com.semanticsoft.patientmobile.data.model.ClinicalPillarType.NUTRITION_VITAMINS,
            reportCount = 1
        )
    )

    private val demoSummary = MarkerSummary(normal = 4, borderline = 2, attention = 2, score = 72)


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
                    if (docs.isEmpty()) {
                        applyDemoDashboardState()
                        _events.emit(DashboardEvent.RefreshCompleted)
                        return@launch
                    }

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
                        attentionItems = demoAttentionItems,
                        basicIndicators = state.basicIndicators.ifEmpty { demoBasicIndicators },
                        markerCategories = state.markerCategories.ifEmpty { demoMarkerCategories },
                        generalMarkerCards = state.generalMarkerCards.ifEmpty { demoGeneralMarkerCards },
                        warningCards = state.warningCards.ifEmpty { demoWarningCards },
                        clinicalPillarCards = state.clinicalPillarCards.ifEmpty { demoClinicalPillarCards },
                        aiSummary = aiSummary,
                        lastAnalysisDate = lastDate,
                        isLoading = false,
                        errorMessage = null
                    )
                    _events.emit(DashboardEvent.RefreshCompleted)
                }

                is Resource.Error -> {
                    applyDemoDashboardState()
                    _events.emit(DashboardEvent.RefreshFailed(docsResource.message))
                }

                Resource.Loading -> Unit
            }
        }
    }

    private fun applyDemoDashboardState() {
        val demoAiSummary = "Date demonstrative pentru vizualizarea dashboard-ului. Conectează contul la analize reale pentru rezultate exacte."
        savedStateHandle[KEY_LAST_ANALYSIS_DATE] = "2026-04-12"
        savedStateHandle[KEY_SUMMARY_NORMAL] = demoSummary.normal
        savedStateHandle[KEY_SUMMARY_BORDERLINE] = demoSummary.borderline
        savedStateHandle[KEY_SUMMARY_ATTENTION] = demoSummary.attention
        savedStateHandle[KEY_SUMMARY_SCORE] = demoSummary.score
        savedStateHandle[KEY_AI_SUMMARY] = demoAiSummary
        savedStateHandle[KEY_ERROR_MESSAGE] = null

        state = state.copy(
            hasUploadedDocuments = true,
            lastAnalysisDate = "2026-04-12",
            attentionItems = demoAttentionItems,
            basicIndicators = demoBasicIndicators,
            markerCategories = demoMarkerCategories,
            generalMarkerCards = demoGeneralMarkerCards,
            markerSummary = demoSummary,
            aiSummary = demoAiSummary,
            warningCards = demoWarningCards,
            clinicalPillarCards = demoClinicalPillarCards,
            isLoading = false,
            errorMessage = null
        )
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
