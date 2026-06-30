package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicationDto(
    val id: String,
    val name: String,
    @SerialName("doseValue") val doseValue: Double,
    @SerialName("doseUnit") val doseUnit: String,
    @SerialName("doseUnitLabel") val doseUnitLabel: String,
    val schedules: List<MedicationScheduleDto> = emptyList(),
    val active: Boolean = true,
    @SerialName("effectiveDate") val effectiveDate: String? = null,
    @SerialName("endDate") val endDate: String? = null,
    @SerialName("analysisDocumentId") val analysisDocumentId: String? = null,
    @SerialName("analysisDocumentName") val analysisDocumentName: String? = null,
    @SerialName("attachments") val attachments: List<MedicalHistoryAttachmentDto> = emptyList(),
    @SerialName("ocrReviewConfirmed") val ocrReviewConfirmed: Boolean = false,
    @SerialName("createdAt") val createdAt: String? = null
)

@Serializable
data class MedicationScheduleDto(
    @SerialName("administrationTime") val administrationTime: String,
    @SerialName("mealRelation") val mealRelation: String,
    @SerialName("mealRelationLabel") val mealRelationLabel: String? = null
)

@Serializable
data class MedicalHistoryAttachmentDto(
    val id: String,
    @SerialName("originalFileName") val originalFileName: String,
    @SerialName("fileType") val fileType: String,
    @SerialName("fileSizeBytes") val fileSizeBytes: Long
)

@Serializable
data class CreateMedicationRequest(
    val name: String,
    @SerialName("doseValue") val doseValue: Double,
    @SerialName("doseUnit") val doseUnit: String,
    val schedules: List<CreateMedicationScheduleRequest>,
    val active: Boolean? = null,
    @SerialName("effectiveDate") val effectiveDate: String? = null,
    @SerialName("endDate") val endDate: String? = null,
    @SerialName("analysisDocumentId") val analysisDocumentId: String? = null,
    @SerialName("attachmentIds") val attachmentIds: List<String>? = null,
    @SerialName("ocrReviewConfirmed") val ocrReviewConfirmed: Boolean? = null
)

@Serializable
data class CreateMedicationScheduleRequest(
    @SerialName("administrationTime") val administrationTime: String,
    @SerialName("mealRelation") val mealRelation: String
)

@Serializable
data class UpdateMedicationRequest(
    val name: String,
    @SerialName("doseValue") val doseValue: Double,
    @SerialName("doseUnit") val doseUnit: String,
    val schedules: List<CreateMedicationScheduleRequest>,
    val active: Boolean? = null,
    @SerialName("effectiveDate") val effectiveDate: String? = null,
    @SerialName("endDate") val endDate: String? = null,
    @SerialName("analysisDocumentId") val analysisDocumentId: String? = null,
    @SerialName("attachmentIds") val attachmentIds: List<String>? = null,
    @SerialName("ocrReviewConfirmed") val ocrReviewConfirmed: Boolean? = null
)

@Serializable
data class PersonalNoteDto(
    val id: String,
    @SerialName("analysisName") val analysisName: String,
    @SerialName("doctorLocation") val doctorLocation: String? = null,
    @SerialName("clinicalObservations") val clinicalObservations: String? = null,
    @SerialName("noteDate") val noteDate: String,
    @SerialName("analysisDocumentId") val analysisDocumentId: String? = null,
    @SerialName("analysisDocumentName") val analysisDocumentName: String? = null,
    @SerialName("attachments") val attachments: List<MedicalHistoryAttachmentDto> = emptyList(),
    @SerialName("createdAt") val createdAt: String? = null
)

@Serializable
data class CreateNoteRequest(
    @SerialName("analysisName") val analysisName: String,
    @SerialName("doctorLocation") val doctorLocation: String? = null,
    @SerialName("clinicalObservations") val clinicalObservations: String? = null,
    @SerialName("noteDate") val noteDate: String,
    @SerialName("analysisDocumentId") val analysisDocumentId: String? = null,
    @SerialName("attachmentIds") val attachmentIds: List<String>? = null
)

@Serializable
data class UpdateNoteRequest(
    @SerialName("analysisName") val analysisName: String,
    @SerialName("doctorLocation") val doctorLocation: String? = null,
    @SerialName("clinicalObservations") val clinicalObservations: String? = null,
    @SerialName("noteDate") val noteDate: String,
    @SerialName("analysisDocumentId") val analysisDocumentId: String? = null,
    @SerialName("attachmentIds") val attachmentIds: List<String>? = null
)
