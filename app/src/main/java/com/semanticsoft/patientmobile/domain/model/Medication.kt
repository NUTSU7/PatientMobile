package com.semanticsoft.patientmobile.domain.model

import java.time.Instant

data class Medication(
    val id: String,
    val name: String,
    val doseValue: Double,
    val doseUnit: DoseUnit,
    val doseUnitLabel: String,
    val schedules: List<MedicationSchedule> = emptyList(),
    val createdAt: Instant? = null
)
