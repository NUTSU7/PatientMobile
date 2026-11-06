package com.estcomputer.patient.feature.patient.dashboard

data class UiMarkerSummary(
    val normal: Int = 0,
    val borderline: Int = 0,
    val attention: Int = 0,
    val score: Int = 0,
    val noReference: Int = 0
)

enum class IndicatorStatus {
    NORMAL,
    BORDERLINE,
    ATTENTION,
    NO_REFERENCE
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
    val id: String = "",
    val title: String,
    val value: String,
    val unit: String,
    val status: IndicatorStatus,
    val trendDirection: IndicatorTrendDirection,
    val trendDelta: String,
    val trendDescription: String,
    val markerPosition: Float,
    val segments: IndicatorSegments = IndicatorSegments(),
    val historyPoints: List<Float> = emptyList(),
    val referenceLow: Float? = null,
    val referenceHigh: Float? = null
)
