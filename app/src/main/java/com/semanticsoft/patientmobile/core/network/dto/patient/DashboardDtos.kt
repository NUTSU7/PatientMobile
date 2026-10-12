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