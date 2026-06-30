package com.semanticsoft.patientmobile.domain.model

import java.time.Instant
import java.time.LocalDate

data class Medication(
    val id: String,
    val name: String,
    val doseValue: Double,
    val doseUnit: DoseUnit,
    val doseUnitLabel: String,
    val schedules: List<MedicationSchedule> = emptyList(),
    val active: Boolean = true,
    val effectiveDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val analysisDocumentId: String? = null,
    val attachmentIds: List<String> = emptyList(),
    val ocrReviewConfirmed: Boolean = false,
    val createdAt: Instant? = null
)
