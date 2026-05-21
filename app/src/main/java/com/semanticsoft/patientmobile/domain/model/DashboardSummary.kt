package com.semanticsoft.patientmobile.domain.model

data class AttentionItem(
    val marker: String,
    val value: String,
    val unit: String,
    val severity: String
)

data class BasicIndicator(
    val title: String,
    val value: String,
    val unit: String,
    val status: String,
    val trendDirection: String? = null,
    val trendDelta: String? = null,
    val trendDescription: String? = null,
    val referenceRange: String? = null
)

data class MarkerCategory(
    val name: String,
    val count: Int
)

data class GeneralMarker(
    val title: String,
    val category: String,
    val value: String,
    val unit: String,
    val status: String,
    val normalRange: String? = null,
    val borderlineRange: String? = null,
    val attentionRange: String? = null
)

data class MarkerSummary(
    val normal: Int,
    val borderline: Int,
    val attention: Int,
    val score: Int
)

data class WarningIndicator(
    val name: String,
    val value: String,
    val unit: String
)

data class WarningCard(
    val level: String,
    val indicators: List<WarningIndicator>
)

data class ClinicalPillarCard(
    val type: String,
    val reportCount: Int,
    val alert: String? = null
)

data class DashboardSummary(
    val greetingName: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val attentionItems: List<AttentionItem> = emptyList(),
    val basicIndicators: List<BasicIndicator> = emptyList(),
    val markerCategories: List<MarkerCategory> = emptyList(),
    val generalMarkers: List<GeneralMarker> = emptyList(),
    val markerSummary: MarkerSummary? = null,
    val aiSummary: String? = null,
    val warningCards: List<WarningCard> = emptyList(),
    val clinicalPillarCards: List<ClinicalPillarCard> = emptyList()
)
