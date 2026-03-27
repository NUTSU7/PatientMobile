package com.estcomputer.patient.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED
}

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ownerUserId"])]
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val uploadedAt: Long,
    val localFilePath: String?,
    val syncStatus: SyncStatus
)
