package com.semanticsoft.patientmobile.domain.model

import java.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MedicalReport(
    val id: String,
    val documentId: String = "",
    val extractionRunId: String? = null,
    val title: String,
    val engineName: String? = null,
    val status: OcrStatus? = null,
    val summary: String? = null,
    @Contextual val createdAt: Instant? = null
)
