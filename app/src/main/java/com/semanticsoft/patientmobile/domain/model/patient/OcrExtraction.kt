package com.semanticsoft.patientmobile.domain.model.patient

import java.time.Instant

data class OcrExtraction(
    val id: String,
    val documentId: String,
    val engineName: String,
    val status: OcrStatus,
    val rawText: String? = null,
    val reports: List<MedicalReport> = emptyList(),
    val createdAt: Instant? = null
)
