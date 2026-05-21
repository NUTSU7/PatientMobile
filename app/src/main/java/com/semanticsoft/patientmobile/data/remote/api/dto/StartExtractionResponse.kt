package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class StartExtractionResponse(
    val runId: String,
    val status: String
)
