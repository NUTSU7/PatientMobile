package com.semanticsoft.patientmobile.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class ReportWithResults(
    val reportId: String,
    val documentId: String,
    val observedAt: LocalDate? = null,
    val clinicalType: String? = null,
    val clinicalSubtype: String? = null,
    val summary: String? = null,
    val requiresReview: Boolean = false,
    val results: List<MedicalResult> = emptyList()
)
