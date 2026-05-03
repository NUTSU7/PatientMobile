package com.semanticsoft.patientmobile.domain.model

import java.time.LocalDate

data class MedicalResult(
    val id: String,
    val documentId: String,
    val reportId: String,
    val analysisType: String,
    val testName: String,
    val value: String,
    val unit: String,
    val referenceRange: String,
    val reportDate: LocalDate
)
