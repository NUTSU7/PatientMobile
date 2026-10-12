package com.semanticsoft.patientmobile.domain.model.patient

data class AiSummary(
    val summaryText: String,
    val status: String,
    val generatedAt: String? = null,
    val latestReportId: String? = null
)
