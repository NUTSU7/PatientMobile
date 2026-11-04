package com.semanticsoft.patientmobile.domain.model.patient

import java.time.Instant
import java.time.LocalDate

data class PersonalNote(
    val id: String,
    val analysisName: String,
    val doctorLocation: String? = null,
    val clinicalObservations: String? = null,
    val noteDate: LocalDate,
    val analysisDocumentId: String? = null,
    val analysisDocumentIds: List<String> = emptyList(),
    val analysisDocuments: List<MedicalHistoryLinkedDocument> = emptyList(),
    val attachmentIds: List<String> = emptyList(),
    val createdAt: Instant? = null
)
