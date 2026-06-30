package com.semanticsoft.patientmobile.domain.model

import java.time.Instant
import java.time.LocalDate

data class PersonalNote(
    val id: String,
    val analysisName: String,
    val doctorLocation: String? = null,
    val clinicalObservations: String? = null,
    val noteDate: LocalDate,
    val analysisDocumentId: String? = null,
    val attachmentIds: List<String> = emptyList(),
    val createdAt: Instant? = null
)
