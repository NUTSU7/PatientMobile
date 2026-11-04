package com.semanticsoft.patientmobile.domain.model.patient

import java.time.Instant
import java.time.LocalDate

data class Medication(
    val id: String,
    val name: String,
    val doseValue: Double? = null,
    val doseUnit: DoseUnit? = null,
    val doseUnitLabel: String? = null,
    val strength: String? = null,
    val frequencyText: String? = null,
    val treatmentId: String? = null,
    val treatmentName: String? = null,
    val schedules: List<MedicationSchedule> = emptyList(),
    val active: Boolean = true,
    val effectiveDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val analysisDocumentId: String? = null,
    val analysisDocumentIds: List<String> = emptyList(),
    val analysisDocuments: List<MedicalHistoryLinkedDocument> = emptyList(),
    val attachmentIds: List<String> = emptyList(),
    val ocrReviewConfirmed: Boolean = false,
    val createdAt: Instant? = null
)
