package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OcrExtractionDto(
    val id: String,
    @SerialName("documentId") val documentId: String,
    @SerialName("engineName") val engineName: String,
    val status: String,
    @SerialName("rawText") val rawText: String? = null,
    val reports: List<OcrReportDto> = emptyList(),
    @SerialName("createdAt") val createdAt: String? = null
)

@Serializable
data class OcrReportDto(
    val id: String,
    val title: String? = null,
    @SerialName("engineName") val engineName: String? = null,
    val status: String? = null,
    val summary: String? = null,
    @SerialName("createdAt") val createdAt: String? = null
)
