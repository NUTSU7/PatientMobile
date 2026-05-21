package com.semanticsoft.patientmobile.domain.model

import java.time.Instant
import java.time.LocalDate

data class PersonalNote(
    val id: String,
    val analysisName: String,
    val doctorLocation: String? = null,
    val clinicalObservations: String? = null,
    val noteDate: LocalDate,
    val createdAt: Instant? = null
)
