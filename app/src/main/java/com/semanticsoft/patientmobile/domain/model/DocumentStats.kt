package com.semanticsoft.patientmobile.domain.model

import java.time.Instant

data class DocumentStats(
    val totalCount: Int,
    val lastUploadedAt: Instant?
)
