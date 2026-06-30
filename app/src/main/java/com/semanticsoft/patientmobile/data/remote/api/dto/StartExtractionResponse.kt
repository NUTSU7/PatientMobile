package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartExtractionResponse(
    @SerialName("id") val runId: String = "",
    val status: String = ""
)
