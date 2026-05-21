package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiSummaryResponse(
    @SerialName("summaryText") val summaryText: String,
    val status: String,
    @SerialName("generatedAt") val generatedAt: String? = null,
    @SerialName("latestReportId") val latestReportId: String? = null
)
