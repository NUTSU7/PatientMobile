package com.semanticsoft.patientmobile.domain.model.laborator

import java.time.Instant

data class LabSubmission(
    val id: String,
    val patientName: String,
    val documentName: String,
    val submittedAt: Instant,
    val status: String
)
