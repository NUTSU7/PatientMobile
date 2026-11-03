package com.semanticsoft.patientmobile.core.network.dto.patient

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiExplainRequest(
    @SerialName("reportId") val reportId: String
)

@Serializable
data class AiExplainResponse(
    val explanation: String
)

@Serializable
data class AiSummaryResponse(
    @SerialName("summaryText") val summaryText: String,
    val status: String,
    @SerialName("generatedAt") val generatedAt: String? = null,
    @SerialName("latestReportId") val latestReportId: String? = null
)
