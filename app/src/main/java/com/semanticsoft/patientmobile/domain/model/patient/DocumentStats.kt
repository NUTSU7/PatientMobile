package com.semanticsoft.patientmobile.domain.model.patient

import java.time.Instant

data class DocumentStats(
    val totalCount: Int,
    val lastUploadedAt: Instant?
)
