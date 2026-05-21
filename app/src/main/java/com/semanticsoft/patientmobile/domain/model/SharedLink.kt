package com.semanticsoft.patientmobile.domain.model

import java.time.Instant

data class SharedLink(
    val token: String,
    val documentId: String,
    val expiresAt: Instant? = null,
    val maxDownloads: Int = 5,
    val downloadCount: Int = 0,
    val downloadUrl: String? = null
)
