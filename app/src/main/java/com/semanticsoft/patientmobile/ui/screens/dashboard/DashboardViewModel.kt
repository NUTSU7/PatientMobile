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
import com.semanticsoft.patientmobile.domain.repository.UploadStateManager
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    val documentCount: Int = 0,
    val aiSummary: String = "",
    val isAiSummaryLoading: Boolean = false,
    val warningCards: List<UiWarningCardItem> = emptyList(),
    val clinicalPillarCards: List<UiClinicalPillarCardItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emptyReason: DashboardEmptyReason? = null
)

enum class DashboardEmptyReason {
    NO_DOCUMENTS,
    PROCESSING,
    NO_RESULTS_EXTRACTED,
    NON_LAB_DOCUMENT
}

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
    private val globalSyncManager: GlobalSyncManager,
    private val uploadStateManager: UploadStateManager
) : ViewModel() {

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(DashboardUiState(isLoading = true))
    val state: StateFlow<DashboardUiState> = combine(
        _state,
        uploadStateManager.isBatchProcessing
    ) { internalState, isProcessing ->
        if (isProcessing) {
            internalState.copy(
                isLoading = false,
                emptyReason = DashboardEmptyReason.PROCESSING,
                hasUploadedDocuments = true
            )
        } else {
            internalState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(isLoading = true))

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
            val resultsDeferred = async { medicalResultRepository.getAllResults(page = 0, size = 100) }
            val aiDeferred = async { dashboardRepository.getAiSummary() }
            val docsDeferred = async { documentRepository.getDocumentStats() }

            val userResult = userDeferred.await()
            val resultsResult = resultsDeferred.await()
            val aiResult = aiDeferred.await()
            val docsResult = docsDeferred.await()

            val docsTotal = if (docsResult is ApiResult.Success) docsResult.data.totalCount else 0
            val hasDocuments = docsTotal > 0
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
                        documentCount = 0,
                        isLoading = false,
                        errorMessage = null,
                        emptyReason = DashboardEmptyReason.NO_DOCUMENTS
                    )
                }
                _events.emit(DashboardEvent.RefreshCompleted)
                return@launch
            }

            if (!hasResults) {
                val checkReason = detectEmptyReason()
                if (checkReason != null) {
                    _state.update {
                        it.copy(
                            greetingName = greetingName.ifBlank { "Pacient" },
                            fullName = fullName,
                            role = role,
                            hasUploadedDocuments = true,
                            documentCount = docsTotal,
                            isLoading = false,
                            errorMessage = null,
                            emptyReason = checkReason
                        )
                    }
                    _events.emit(DashboardEvent.RefreshCompleted)
                    if (checkReason == DashboardEmptyReason.PROCESSING) {
                        launch {
                            delay(5_000L)
                            refresh()
                        }
                    }
                    return@launch
                }
            }

            val results = if (resultsResult is ApiResult.Success) resultsResult.data else emptyList()

            buildCardsFromResults(greetingName, fullName, role, results, aiResult, docsTotal)

            if (hasResults) {
                val historyResults = loadFrequentIndicatorsHistory()
                if (historyResults.isNotEmpty()) {
                    val updatedIndicators = buildBasicIndicators(historyResults)
                    _state.update { it.copy(basicIndicators = updatedIndicators) }
                }
            }

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
                        else -> {
                            _state.update { it.copy(isAiSummaryLoading = true, aiSummary = "") }
                            delay(5_000L)
                            regenerateAiSummary()
                        }
                    }
                }
                else -> _state.update { it.copy(isAiSummaryLoading = false) }
            }
        }
    }

    private suspend fun detectEmptyReason(): DashboardEmptyReason? {
        val docsResult = documentRepository.getDocuments(page = 0, size = 5, sortBy = "uploadedAt", sortDir = "desc")
        if (docsResult !is ApiResult.Success) return DashboardEmptyReason.NO_RESULTS_EXTRACTED
        val docs = docsResult.data
        if (docs.isEmpty()) return DashboardEmptyReason.NO_RESULTS_EXTRACTED
        val anyReady = docs.any { it.observedAt != null }
        return if (anyReady) null else DashboardEmptyReason.PROCESSING
    }

    private fun buildCardsFromResults(
        greetingName: String,
        fullName: String,
        role: String,
        results: List<MedicalResult>,
        aiResult: ApiResult<com.semanticsoft.patientmobile.data.remote.api.dto.AiSummaryResponse>,
        documentCount: Int
    ) {
        val abnormal = results.filter { !it.abnormalFlag.isNullOrBlank() }
        val categories = results.groupBy { it.analysisGroup.ifBlank { "Altele" } }

        val summary = buildSummary(results)
        val indicators = buildBasicIndicators(results)
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
                else -> {
                    val currentSummary = _state.value.aiSummary
                    if (currentSummary.isNotBlank()) {
                        aiText = currentSummary
                    } else {
                        aiLoading = true
                    }
                    autoRegenerateAiSummary()
                }
            }
        } else if (results.isNotEmpty()) {
            aiLoading = true
            autoRegenerateAiSummary()
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
                generalMarkerCards = emptyList(),
                markerSummary = summary,
                documentCount = documentCount,
                aiSummary = aiText,
                isAiSummaryLoading = aiLoading,
                warningCards = warnings,
                clinicalPillarCards = pillars,
                isLoading = false,
                errorMessage = null,
                emptyReason = null
            )
        }
    }

    private fun buildSummary(results: List<MedicalResult>): UiMarkerSummary {
        if (results.isEmpty()) {
            return UiMarkerSummary(normal = 0, borderline = 0, attention = 0, score = 0, noReference = 0)
        }
        var normal = 0
        var borderline = 0
        var attention = 0
        var noReference = 0
        for (r in results) {
            when (parseStatus(r)) {
                IndicatorStatus.NORMAL -> normal++
                IndicatorStatus.BORDERLINE -> borderline++
                IndicatorStatus.ATTENTION -> attention++
                IndicatorStatus.NO_REFERENCE -> noReference++
            }
        }
        val interpretable = normal + borderline + attention
        val score = if (interpretable > 0) kotlin.math.round(normal.toDouble() / interpretable * 100).toInt() else 0
        return UiMarkerSummary(normal = normal, borderline = borderline, attention = attention, score = score, noReference = noReference)
    }

    private fun buildBasicIndicators(
        results: List<MedicalResult>
    ): List<UiBasicIndicatorItem> {
        if (results.isEmpty()) return emptyList()

        val grouped = results.groupBy { getIndicatorKey(it) }

        data class GroupData(
            val displayLabel: String,
            val frequency: Int,
            val latestObservedAt: java.time.LocalDate?,
            val items: List<MedicalResult>
        )

        val enriched = grouped.values.map { items ->
            val sorted = items.sortedByDescending { it.observedAt }
            val first = sorted.first()
            GroupData(
                displayLabel = first.canonicalName.ifBlank { first.originalTestName },
                frequency = items.size,
                latestObservedAt = sorted.mapNotNull { it.observedAt }.maxOrNull(),
                items = sorted
            )
        }

        val sortedGroups = enriched.sortedWith(
            compareByDescending<GroupData> { it.frequency }
                .thenByDescending { it.latestObservedAt }
                .thenBy { it.displayLabel }
        )

        return sortedGroups.map { group ->
            val r = group.items.first()
            val numericValues = group.items
                .sortedBy { it.observedAt }
                .mapNotNull { it.valueNumeric?.toFloat() }
            val sampled = sampleHistoryPoints(numericValues, 10)
            val ref = resolveReferenceRange(group.items)
            UiBasicIndicatorItem(
                title = group.displayLabel,
                value = r.valueNumeric?.toString() ?: r.valueText ?: "-",
                unit = r.unit,
                status = parseStatus(r),
                trendDirection = IndicatorTrendDirection.STABLE,
                trendDelta = "",
                trendDescription = r.referenceText ?: "",
                markerPosition = 0.5f,
                segments = IndicatorSegments(),
                historyPoints = sampled,
                referenceLow = ref.first,
                referenceHigh = ref.second
            )
        }
    }

    private fun getIndicatorKey(result: MedicalResult): String {
        return if (!result.testDefinitionId.isNullOrBlank()) {
            "definition:${result.testDefinitionId}"
        } else {
            val displayName = result.canonicalName.ifBlank { result.originalTestName }
            "name:${normalizeForGrouping(displayName)}"
        }
    }

    private fun sampleHistoryPoints(values: List<Float>, limit: Int): List<Float> {
        if (values.isEmpty()) return emptyList()
        if (values.size <= limit) return values
        val result = mutableListOf<Float>()
        for (i in 0 until limit) {
            val idx = kotlin.math.round(i * (values.size - 1).toFloat() / (limit - 1).toFloat()).toInt()
            result.add(values[idx])
        }
        return result
    }

    private fun resolveReferenceRange(items: List<MedicalResult>): Pair<Float?, Float?> {
        for (item in items) {
            if (item.referenceLow != null && item.referenceHigh != null) {
                return Pair(item.referenceLow.toFloat(), item.referenceHigh.toFloat())
            }
        }
        return Pair(null, null)
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

    private fun parseStatus(result: MedicalResult): IndicatorStatus {
        val flag = result.abnormalFlag
        if (flag.isNullOrBlank()) {
            return if (hasValidReferenceRange(result)) IndicatorStatus.NORMAL else IndicatorStatus.NO_REFERENCE
        }
        return when (flag.uppercase()) {
            "NORMAL" -> if (hasValidReferenceRange(result)) IndicatorStatus.NORMAL else IndicatorStatus.NO_REFERENCE
            "LOW", "HIGH" -> IndicatorStatus.BORDERLINE
            "ABNORMAL", "ATTENTION", "CRITICAL" -> IndicatorStatus.ATTENTION
            "BORDERLINE", "WARNING" -> IndicatorStatus.BORDERLINE
            else -> if (hasValidReferenceRange(result)) IndicatorStatus.NORMAL else IndicatorStatus.NO_REFERENCE
        }
    }

    private fun hasValidReferenceRange(result: MedicalResult): Boolean {
        return result.referenceLow != null && result.referenceHigh != null
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

    private suspend fun loadFrequentIndicatorsHistory(): List<MedicalResult> {
        val all = mutableListOf<MedicalResult>()
        var page = 0
        val maxPages = 50
        do {
            val result = medicalResultRepository.getAllResultsPaginated(
                page = page,
                size = 100,
                sortBy = "observedAt",
                sortDir = "desc"
            )
            if (result !is ApiResult.Success) break
            val pageData = result.data
            all.addAll(pageData.items)
            page++
            if (!pageData.hasNext) break
        } while (page < maxPages)
        return all
    }

    private fun normalizeForGrouping(name: String): String {
        val nfd = Normalizer.normalize(name, Normalizer.Form.NFD)
        val stripped = nfd.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        val roFallback = stripped
            .replace('ș', 's').replace('Ş', 's')
            .replace('ț', 't').replace('Ț', 't')
            .replace('ă', 'a').replace('Ă', 'a')
            .replace('â', 'a').replace('Â', 'a')
            .replace('î', 'i').replace('Î', 'i')
        return roFallback.lowercase().replace(Regex("\\s+"), " ").trim()
    }

    private fun autoRegenerateAiSummary() {
        viewModelScope.launch {
            when (val result = dashboardRepository.regenerateAiSummary()) {
                is ApiResult.Success -> {
                    val response = result.data
                    when (response.status.uppercase()) {
                        "READY", "COMPLETED" -> _state.update {
                            it.copy(aiSummary = response.summaryText, isAiSummaryLoading = false)
                        }
                        else -> { }
                    }
                }
                else -> _state.update { it.copy(isAiSummaryLoading = false) }
            }
        }
    }
}
