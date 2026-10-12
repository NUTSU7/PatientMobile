package com.semanticsoft.patientmobile.domain.model.laborator

data class LabResult(
    val id: String,
    val submissionId: String,
    val marker: String,
    val value: String,
    val unit: String
)
