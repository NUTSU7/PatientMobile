package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.data.model.ClinicalPillarType
import com.semanticsoft.patientmobile.data.model.IndicatorSegments
import com.semanticsoft.patientmobile.data.model.IndicatorStatus
import com.semanticsoft.patientmobile.data.model.IndicatorTrendDirection
import com.semanticsoft.patientmobile.data.model.WarningLevel
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
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
import com.semanticsoft.patientmobile.data.model.AttentionItem as UiAttentionItem
import com.semanticsoft.patientmobile.data.model.BasicIndicatorItem as UiBasicIndicatorItem
import com.semanticsoft.patientmobile.data.model.ClinicalPillarCardItem as UiClinicalPillarCardItem
import com.semanticsoft.patientmobile.data.model.GeneralMarkerCardItem as UiGeneralMarkerCardItem
import com.semanticsoft.patientmobile.data.model.MarkerCategoryItem as UiMarkerCategoryItem
import com.semanticsoft.patientmobile.data.model.MarkerSummary as UiMarkerSummary
import com.semanticsoft.patientmobile.data.model.WarningCardItem as UiWarningCardItem
import com.semanticsoft.patientmobile.data.model.WarningIndicatorItem as UiWarningIndicatorItem

data class DashboardUiState(
    val greetingName: String = "",
    val fullName: String = "",
    val role: String = "",
    val profilePhotoResId: Int? = null,
    val hasUploadedDocuments: Boolean = false,
    val lastAnalysisDate: String = "",
    val attentionItems: List<UiAttentionItem> = emptyList(),
    val basicIndicators: List<UiBasicIndicatorItem> = emptyList(),
    val markerCategories: List<UiMarkerCategoryItem> = emptyList(),
    val generalMarkerCards: List<UiGeneralMarkerCardItem> = emptyList(),
    val markerSummary: UiMarkerSummary = UiMarkerSummary(0, 0, 0, 0),
    val aiSummary: String = "",
    val isAiSummaryLoading: Boolean = false,
    val warningCards: List<UiWarningCardItem> = emptyList(),
    val clinicalPillarCards: List<UiClinicalPillarCardItem> = emptyList(),
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
    private val dashboardRepository: DashboardRepository,
    private val medicalResultRepository: MedicalResultRepository,
    private val documentRepository: DocumentRepository,
    private val globalSyncManager: GlobalSyncManager
) : ViewModel() {

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(DashboardUiState(isLoading = true))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private val _refreshErrors = MutableSharedFlow<String>()
    val refreshErrors: SharedFlow<String> = _refreshErrors.asSharedFlow()

    init {
        viewModelScope.launch {
            globalSyncManager.syncEvents.collect { refresh() }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _events.emit(DashboardEvent.LogoutSuccess)
            } catch (e: Exception) {
                val message = e.message ?: "Deconectarea a e\u0219uat."
                _events.emit(DashboardEvent.LogoutFailure(message))
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val userDeferred = async { authRepository.getCurrentUser() }
            val resultsDeferred = async { medicalResultRepository.getLatestResults() }
            val aiDeferred = async { dashboardRepository.getAiSummary() }
            val docsDeferred = async { documentRepository.getDocuments(page = 0, size = 1) }

            val userResult = userDeferred.await()
            val resultsResult = resultsDeferred.await()
            val aiResult = aiDeferred.await()
            val docsResult = docsDeferred.await()

            val hasDocuments = docsResult is ApiResult.Success && docsResult.data.isNotEmpty()
            val hasResults = resultsResult is ApiResult.Success && resultsResult.data.isNotEmpty()

            var greetingName = ""
            var fullName = ""
            var role = "Pacient"

            if (userResult is ApiResult.Success) {
                val user = userResult.data
                greetingName = user.firstName.ifBlank { user.email.substringBefore("@") }
                fullName = listOfNotNull(user.firstName, user.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ").ifBlank { user.email }
                role = user.role.ifBlank { "Pacient" }
            }

            if (!hasDocuments && !hasResults) {
                _state.update {
                    it.copy(
                        greetingName = greetingName.ifBlank { "Pacient" },
                        fullName = fullName,
                        role = role,
                        hasUploadedDocuments = false,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                _events.emit(DashboardEvent.RefreshCompleted)
                return@launch
            }

            val results = if (resultsResult is ApiResult.Success) resultsResult.data else emptyList()

            buildCardsFromResults(greetingName, fullName, role, results, aiResult)

            _events.emit(DashboardEvent.RefreshCompleted)
        }
    }

    fun regenerateAiSummary() {
        viewModelScope.launch {
            _state.update { it.copy(isAiSummaryLoading = true, aiSummary = "") }
            when (val result = dashboardRepository.regenerateAiSummary()) {
                is ApiResult.Success -> {
                    val response = result.data
                    when (response.status.uppercase()) {
                        "READY", "COMPLETED" -> _state.update {
                            it.copy(aiSummary = response.summaryText, isAiSummaryLoading = false)
                        }
                        else -> _state.update { it.copy(isAiSummaryLoading = true, aiSummary = "") }
                    }
                }
                else -> _state.update { it.copy(isAiSummaryLoading = false) }
            }
        }
    }

    private fun buildCardsFromResults(
        greetingName: String,
        fullName: String,
        role: String,
        results: List<MedicalResult>,
        aiResult: ApiResult<com.semanticsoft.patientmobile.data.remote.api.dto.AiSummaryResponse>
    ) {
        val abnormal = results.filter { !it.abnormalFlag.isNullOrBlank() }
        val categories = results.groupBy { it.analysisGroup.ifBlank { "Altele" } }
        val knownCanonicals = setOf(
            "WBC", "Hemoglobin", "HCT", "PLT", "RBC",
            "Glucose", "ALT", "AST", "Creatinine", "Urea",
            "TSH", "Vitamin D", "Iron", "Ferritin", "Cholesterol"
        )

        val summary = buildSummary(results)
        val indicators = buildBasicIndicators(results, knownCanonicals)
        val markers = buildGeneralMarkers(results)
        val markersByCategory = buildMarkerCategories(categories)
        val attention = buildAttentionItems(abnormal)
        val warnings = buildWarningCards(abnormal)
        val pillars = buildClinicalPillars(categories)

        var aiText = ""
        var aiLoading = false

        if (aiResult is ApiResult.Success) {
            val ai = aiResult.data
            when (ai.status.uppercase()) {
                "READY", "COMPLETED" -> aiText = ai.summaryText
                else -> aiLoading = true
            }
        }

        _state.update {
            it.copy(
                greetingName = greetingName,
                fullName = fullName,
                role = role,
                hasUploadedDocuments = true,
                attentionItems = attention,
                basicIndicators = indicators,
                markerCategories = markersByCategory,
                generalMarkerCards = markers,
                markerSummary = summary,
                aiSummary = aiText,
                isAiSummaryLoading = aiLoading,
                warningCards = warnings,
                clinicalPillarCards = pillars,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun buildSummary(results: List<MedicalResult>): UiMarkerSummary {
        val normal = results.count { it.abnormalFlag.isNullOrBlank() }
        val abnormal = results.size - normal
        val borderline = results.count {
            it.abnormalFlag?.uppercase()?.let { it == "BORDERLINE" || it == "WARNING" } == true
        }
        val attention = abnormal - borderline
        val score = if (results.isNotEmpty()) (normal * 100 / results.size) else 72
        return UiMarkerSummary(normal = normal, borderline = borderline, attention = attention, score = score)
    }

    private fun buildBasicIndicators(
        results: List<MedicalResult>,
        knownCanonicals: Set<String>
    ): List<UiBasicIndicatorItem> {
        return results
            .filter { it.canonicalName.ifBlank { it.originalTestName } in knownCanonicals }
            .take(10)
            .map { r ->
                UiBasicIndicatorItem(
                    title = r.originalTestName.ifBlank { r.canonicalName },
                    value = r.valueNumeric?.toString() ?: r.valueText ?: "-",
                    unit = r.unit,
                    status = parseStatus(r.abnormalFlag),
                    trendDirection = IndicatorTrendDirection.STABLE,
                    trendDelta = "",
                    trendDescription = r.referenceText ?: "",
                    markerPosition = 0.5f,
                    segments = IndicatorSegments()
                )
            }
    }

    private fun buildGeneralMarkers(results: List<MedicalResult>): List<UiGeneralMarkerCardItem> {
        return results.map { r ->
            UiGeneralMarkerCardItem(
                title = r.originalTestName.ifBlank { r.canonicalName },
                category = r.analysisGroup.ifBlank { "Altele" },
                value = r.valueNumeric?.toString() ?: r.valueText ?: "-",
                unit = r.unit,
                status = parseStatus(r.abnormalFlag),
                normalRange = r.referenceText ?: buildRangeLabel(r.referenceLow, r.referenceHigh),
                borderlineRange = "-",
                attentionRange = "-"
            )
        }
    }

    private fun buildMarkerCategories(categories: Map<String, List<MedicalResult>>): List<UiMarkerCategoryItem> {
        return categories.map { (group, items) ->
            UiMarkerCategoryItem(name = group, count = items.size)
        }
    }

    private fun buildAttentionItems(abnormal: List<MedicalResult>): List<UiAttentionItem> {
        return abnormal.map { r ->
            UiAttentionItem(
                marker = r.originalTestName.ifBlank { r.canonicalName },
                value = r.valueNumeric?.toString() ?: r.valueText ?: "-",
                unit = r.unit,
                severity = r.abnormalFlag ?: "Aten\u021Bie"
            )
        }
    }

    private fun buildWarningCards(abnormal: List<MedicalResult>): List<UiWarningCardItem> {
        if (abnormal.isEmpty()) return emptyList()

        val highFlags = listOf("HIGH", "ATTENTION", "ABNORMAL", "CRITICAL")
        val highItems = abnormal.filter {
            it.abnormalFlag?.uppercase() in highFlags
        }
        val moderateItems = abnormal.filter {
            it.abnormalFlag?.uppercase() !in highFlags
        }

        val cards = mutableListOf<UiWarningCardItem>()
        if (highItems.isNotEmpty()) {
            cards.add(
                UiWarningCardItem(
                    level = WarningLevel.HIGH,
                    indicators = highItems.map { r ->
                        UiWarningIndicatorItem(
                            name = r.originalTestName.ifBlank { r.canonicalName },
                            value = r.valueNumeric?.toString() ?: r.valueText ?: "-",
                            unit = r.unit
                        )
                    }
                )
            )
        }
        if (moderateItems.isNotEmpty()) {
            cards.add(
                UiWarningCardItem(
                    level = WarningLevel.MODERATE,
                    indicators = moderateItems.map { r ->
                        UiWarningIndicatorItem(
                            name = r.originalTestName.ifBlank { r.canonicalName },
                            value = r.valueNumeric?.toString() ?: r.valueText ?: "-",
                            unit = r.unit
                        )
                    }
                )
            )
        }
        return cards
    }

    private fun buildClinicalPillars(categories: Map<String, List<MedicalResult>>): List<UiClinicalPillarCardItem> {
        return categories.map { (group, items) ->
            UiClinicalPillarCardItem(
                type = parsePillarType(group),
                reportCount = items.size
            )
        }
    }

    private fun buildRangeLabel(low: Double?, high: Double?): String {
        if (low == null && high == null) return "-"
        val lowStr = low?.toString() ?: ""
        val highStr = high?.toString() ?: ""
        return "$lowStr - $highStr".trim()
    }

    private fun parseStatus(abnormalFlag: String?): IndicatorStatus {
        if (abnormalFlag.isNullOrBlank()) return IndicatorStatus.NORMAL
        return when (abnormalFlag.uppercase()) {
            "HIGH", "LOW", "ATTENTION", "ABNORMAL", "CRITICAL" -> IndicatorStatus.ATTENTION
            "BORDERLINE", "WARNING" -> IndicatorStatus.BORDERLINE
            else -> IndicatorStatus.NORMAL
        }
    }

    private fun parsePillarType(analysisGroup: String): ClinicalPillarType {
        return when (analysisGroup.uppercase()) {
            "HEMATOLOGY", "HEMATOLOGIE" -> ClinicalPillarType.BLOOD_CELLS
            "BIOCHEMISTRY", "BIOCHIMIE" -> ClinicalPillarType.ORGANS_METABOLISM
            "CARDIOLOGY", "CARDIOLOGIE", "LIPIDS", "LIPIDE" -> ClinicalPillarType.HEART_CV
            "HORMONES", "HORMONI", "THYROID", "TIROIDA" -> ClinicalPillarType.HORMONES
            "ONCOLOGY", "ONCOLOGIE", "TUMOR_MARKERS" -> ClinicalPillarType.ONCOLOGY_MARKERS
            "VITAMINS", "VITAMINE", "NUTRITION", "NUTRITIE" -> ClinicalPillarType.NUTRITION_VITAMINS
            "COAGULATION", "COAGULARE" -> ClinicalPillarType.COAGULATION
            "IMMUNOLOGY", "IMUNOLOGIE", "INFECTIONS" -> ClinicalPillarType.INFECTIONS_IMMUNOLOGY
            else -> ClinicalPillarType.ORGANS_METABOLISM
        }
    }
}
