package com.estcomputer.patient.domain.model

import java.time.Instant

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED
}

data class PatientDocument(
    val id: String,
    val ownerUserId: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val uploadedAt: Instant,
    val localFilePath: String? = null,
    val syncStatus: SyncStatus
)
