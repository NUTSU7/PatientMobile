package com.estcomputer.patient.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["userId"]), Index(value = ["timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val action: String,
    val resourceType: String,
    val resourceId: String,
    val timestamp: Long,
    val status: String
)
