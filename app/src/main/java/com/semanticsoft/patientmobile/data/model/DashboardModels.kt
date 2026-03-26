package com.semanticsoft.patientmobile.data.model

data class AttentionItem(
    val marker: String,
    val value: String,
    val unit: String,
    val severity: String
)

data class MarkerSummary(
    val normal: Int,
    val borderline: Int,
    val attention: Int,
    val score: Int
)

enum class IndicatorStatus {
    NORMAL,
    BORDERLINE,
    ATTENTION
}

enum class IndicatorTrendDirection {
    STABLE,
    UP,
    DOWN
}

data class IndicatorSegments(
    val good: Float = 0.6f,
    val borderline: Float = 0.2f,
    val risk: Float = 0.2f
)

data class BasicIndicatorItem(
    val title: String,
    val value: String,
    val unit: String,
    val status: IndicatorStatus,
    val trendDirection: IndicatorTrendDirection,
    val trendDelta: String,
    val trendDescription: String,
    val markerPosition: Float,
    val segments: IndicatorSegments = IndicatorSegments()
)

data class MarkerCategoryItem(
    val name: String,
    val count: Int
)

data class GeneralMarkerCardItem(
    val title: String,
    val category: String,
    val value: String,
    val unit: String,
    val status: IndicatorStatus,
    val normalRange: String,
    val borderlineRange: String,
    val attentionRange: String
)
