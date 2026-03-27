package com.estcomputer.patient.domain.model

import java.time.Instant
import java.time.LocalDate

data class MedicalReport(
    val id: String,
    val documentId: String,
    val extractionRunId: String?,
    val reportTitle: String,
    val vendorName: String,
    val vendorLocation: String?,
    val collectedAt: Instant?,
    val reportDate: LocalDate,
    val patientName: String,
    val patientDob: LocalDate?,
    val patientSex: String?
)
