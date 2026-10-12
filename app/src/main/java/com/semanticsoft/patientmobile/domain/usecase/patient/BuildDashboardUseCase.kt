package com.semanticsoft.patientmobile.domain.usecase.patient

import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.domain.model.patient.BasicIndicatorItem
import com.semanticsoft.patientmobile.domain.model.patient.ClinicalPillarCardItem
import com.semanticsoft.patientmobile.domain.model.patient.ClinicalPillarType
import com.semanticsoft.patientmobile.domain.model.patient.IndicatorSegments
import com.semanticsoft.patientmobile.domain.model.patient.IndicatorStatus
import com.semanticsoft.patientmobile.domain.model.patient.IndicatorTrendDirection
import com.semanticsoft.patientmobile.domain.model.patient.MarkerCategoryItem
import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.domain.model.patient.UiAttentionItem
import com.semanticsoft.patientmobile.domain.model.patient.UiMarkerSummary
import com.semanticsoft.patientmobile.domain.model.patient.WarningLevel
import com.semanticsoft.patientmobile.domain.model.patient.WarningCardItem
import com.semanticsoft.patientmobile.domain.model.patient.WarningIndicatorItem
import java.text.Normalizer
import javax.inject.Inject

data class DashboardCardData(
    val attentionItems: List<UiAttentionItem>,
    val markerCategories: List<MarkerCategoryItem>,
    val markerSummary: UiMarkerSummary,
    val warningCards: List<WarningCardItem>,
    val clinicalPillarCards: List<ClinicalPillarCardItem>
)

class BuildDashboardUseCase @Inject constructor() {

    operator fun invoke(results: List<MedicalResult>): DashboardCardData {
        val abnormal = results.filter { !it.abnormalFlag.isNullOrBlank() }
        val categories = results.groupBy { it.analysisGroup.ifBlank { "Altele" } }

        return DashboardCardData(
            attentionItems = buildAttentionItems(abnormal),
            markerCategories = buildMarkerCategories(categories),
            markerSummary = buildSummary(results),
            warningCards = buildWarningCards(abnormal),
            clinicalPillarCards = buildClinicalPillars(categories)
        )
    }

    fun buildBasicIndicators(results: List<MedicalResult>): List<BasicIndicatorItem> {
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
            BasicIndicatorItem(
                id = getIndicatorKey(r),
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

    fun buildResultsSignature(
        results: List<MedicalResult>,
        aiResult: ApiResult<AiSummary>,
        documentCount: Int
    ): String {
        val resultsPart = results.joinToString("|") { r ->
            listOf(
                r.id,
                r.valueNumeric?.toString().orEmpty(),
                r.valueText.orEmpty(),
                r.unit,
                r.abnormalFlag.orEmpty(),
                r.observedAt?.toString().orEmpty(),
                r.reportId.orEmpty()
            ).joinToString(",")
        }
        val aiPart = when (aiResult) {
            is ApiResult.Success -> "${aiResult.data.status}:${aiResult.data.summaryText}"
            is ApiResult.HttpError -> "http:${aiResult.code}"
            is ApiResult.NetworkError -> "network"
            is ApiResult.AuthError -> "auth"
        }
        return "$documentCount::$resultsPart::$aiPart"
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

    private fun buildMarkerCategories(categories: Map<String, List<MedicalResult>>): List<MarkerCategoryItem> {
        return categories.map { (group, items) ->
            MarkerCategoryItem(name = group, count = items.size)
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

    private fun buildWarningCards(abnormal: List<MedicalResult>): List<WarningCardItem> {
        if (abnormal.isEmpty()) return emptyList()

        val highFlags = listOf("HIGH", "ATTENTION", "ABNORMAL", "CRITICAL")
        val highItems = abnormal.filter {
            it.abnormalFlag?.uppercase() in highFlags
        }
        val moderateItems = abnormal.filter {
            it.abnormalFlag?.uppercase() !in highFlags
        }

        val cards = mutableListOf<WarningCardItem>()
        if (highItems.isNotEmpty()) {
            cards.add(
                WarningCardItem(
                    level = WarningLevel.HIGH,
                    indicators = highItems.map { r ->
                        WarningIndicatorItem(
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
                WarningCardItem(
                    level = WarningLevel.MODERATE,
                    indicators = moderateItems.map { r ->
                        WarningIndicatorItem(
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

    private fun buildClinicalPillars(categories: Map<String, List<MedicalResult>>): List<ClinicalPillarCardItem> {
        return categories.map { (group, items) ->
            ClinicalPillarCardItem(
                type = parsePillarType(group),
                reportCount = items.size
            )
        }
    }

    private fun parseStatus(result: MedicalResult): IndicatorStatus {
        val hasRef = hasValidReferenceRange(result)
        if (!hasRef) return IndicatorStatus.NO_REFERENCE
        val flag = result.abnormalFlag
        if (flag.isNullOrBlank()) return IndicatorStatus.NORMAL
        return when (flag.uppercase()) {
            "NORMAL" -> IndicatorStatus.NORMAL
            "LOW", "HIGH", "BORDERLINE", "WARNING" -> IndicatorStatus.BORDERLINE
            "ABNORMAL", "ATTENTION", "CRITICAL" -> IndicatorStatus.ATTENTION
            else -> IndicatorStatus.NORMAL
        }
    }

    private fun hasValidReferenceRange(result: MedicalResult): Boolean {
        val low = result.referenceLow
        val high = result.referenceHigh
        if (low != null && high != null) {
            return high > low
        }
        return low != null || high != null
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
}
