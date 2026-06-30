package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicalResultDto(
    val id: String? = null,
    @SerialName("testDefinitionId") val testDefinitionId: String? = null,
    @SerialName("documentId") val documentId: String,
    @SerialName("reportId") val reportId: String? = null,
    @SerialName("originalTestName") val originalTestName: String,
    @SerialName("canonicalName") val canonicalName: String,
    @SerialName("analysisGroup") val analysisGroup: String,
    @SerialName("valueNumeric") val valueNumeric: Double? = null,
    @SerialName("valueText") val valueText: String? = null,
    val unit: String,
    @SerialName("referenceLow") val referenceLow: Double? = null,
    @SerialName("referenceHigh") val referenceHigh: Double? = null,
    @SerialName("referenceText") val referenceText: String? = null,
    @SerialName("abnormalFlag") val abnormalFlag: String? = null,
    @SerialName("observedAt") val observedAt: String? = null
)

@Serializable
data class MedicalResultHistoryEntry(
    val id: String,
    @SerialName("originalTestName") val originalTestName: String,
    @SerialName("valueNumeric") val valueNumeric: Double? = null,
    @SerialName("valueText") val valueText: String? = null,
    val unit: String,
    @SerialName("observedAt") val observedAt: String? = null,
    @SerialName("abnormalFlag") val abnormalFlag: String? = null
)

@Serializable
data class MedicalReportWithResultsDto(
    @SerialName("reportId") val reportId: String,
    @SerialName("documentId") val documentId: String,
    @SerialName("observedAt") val observedAt: String? = null,
    @SerialName("clinicalType") val clinicalType: String? = null,
    @SerialName("clinicalSubtype") val clinicalSubtype: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("requiresReview") val requiresReview: Boolean = false,
    val results: List<MedicalResultItemDto> = emptyList()
)

@Serializable
data class MedicalResultItemDto(
    val id: String,
    @SerialName("analysisGroup") val analysisGroup: String = "",
    @SerialName("sortOrder") val sortOrder: Int? = null,
    @SerialName("originalTestName") val originalTestName: String,
    @SerialName("testDefinitionId") val testDefinitionId: String? = null,
    @SerialName("canonicalName") val canonicalName: String = "",
    @SerialName("valueNumeric") val valueNumeric: Double? = null,
    @SerialName("valueText") val valueText: String? = null,
    val unit: String = "",
    @SerialName("referenceLow") val referenceLow: Double? = null,
    @SerialName("referenceHigh") val referenceHigh: Double? = null,
    @SerialName("referenceText") val referenceText: String? = null,
    @SerialName("abnormalFlag") val abnormalFlag: String? = null,
    @SerialName("observedAt") val observedAt: String? = null
)
