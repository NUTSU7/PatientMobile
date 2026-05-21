package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummaryDto(
    @SerialName("greetingName") val greetingName: String? = null,
    @SerialName("fullName") val fullName: String? = null,
    val role: String? = null,
    @SerialName("attentionItems") val attentionItems: List<AttentionItemDto> = emptyList(),
    @SerialName("basicIndicators") val basicIndicators: List<BasicIndicatorDto> = emptyList(),
    @SerialName("markerCategories") val markerCategories: List<MarkerCategoryDto> = emptyList(),
    @SerialName("generalMarkers") val generalMarkers: List<GeneralMarkerDto> = emptyList(),
    @SerialName("markerSummary") val markerSummary: MarkerSummaryDto? = null,
    @SerialName("aiSummary") val aiSummary: String? = null,
    @SerialName("warningCards") val warningCards: List<WarningCardDto> = emptyList(),
    @SerialName("clinicalPillarCards") val clinicalPillarCards: List<ClinicalPillarCardDto> = emptyList()
)

@Serializable
data class AttentionItemDto(
    val marker: String,
    val value: String,
    val unit: String,
    val severity: String
)

@Serializable
data class BasicIndicatorDto(
    val title: String,
    val value: String,
    val unit: String,
    val status: String,
    @SerialName("trendDirection") val trendDirection: String? = null,
    @SerialName("trendDelta") val trendDelta: String? = null,
    @SerialName("trendDescription") val trendDescription: String? = null,
    @SerialName("referenceRange") val referenceRange: String? = null
)

@Serializable
data class MarkerCategoryDto(
    val name: String,
    val count: Int
)

@Serializable
data class GeneralMarkerDto(
    val title: String,
    val category: String,
    val value: String,
    val unit: String,
    val status: String,
    @SerialName("normalRange") val normalRange: String? = null,
    @SerialName("borderlineRange") val borderlineRange: String? = null,
    @SerialName("attentionRange") val attentionRange: String? = null
)

@Serializable
data class MarkerSummaryDto(
    val normal: Int,
    val borderline: Int,
    val attention: Int,
    val score: Int
)

@Serializable
data class WarningIndicatorDto(
    val name: String,
    val value: String,
    val unit: String
)

@Serializable
data class WarningCardDto(
    val level: String,
    val indicators: List<WarningIndicatorDto>
)

@Serializable
data class ClinicalPillarCardDto(
    val type: String,
    @SerialName("reportCount") val reportCount: Int,
    val alert: String? = null
)

@Serializable
data class AiExplainRequest(
    @SerialName("reportId") val reportId: String
)

@Serializable
data class AiExplainResponse(
    val explanation: String
)
