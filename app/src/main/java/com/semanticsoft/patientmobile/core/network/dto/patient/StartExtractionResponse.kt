package com.semanticsoft.patientmobile.core.network.dto.patient

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartExtractionResponse(
    @SerialName("id") val runId: String = "",
    val status: String = ""
)
