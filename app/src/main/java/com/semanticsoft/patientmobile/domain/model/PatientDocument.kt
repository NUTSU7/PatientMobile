package com.semanticsoft.patientmobile.domain.model

import java.time.Instant

data class PatientDocument(
    val id: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val uploadedAt: Instant,
    val observedAt: Instant? = null,
    val categories: List<String> = emptyList(),
    val sha256Checksum: String? = null,
    val localFilePath: String? = null
)
