package com.semanticsoft.patientmobile.domain.model.patient

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
    val displayTitle: String? = null,
    val displaySubtitle: String? = null,
    val sourceLaboratory: String? = null,
    val reviewStatus: String? = null,
    val reviewedAt: java.time.Instant? = null,
    val results: List<MedicalResult> = emptyList()
)
