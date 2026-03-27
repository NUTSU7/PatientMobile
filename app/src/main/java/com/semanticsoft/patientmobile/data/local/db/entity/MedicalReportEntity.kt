package com.semanticsoft.patientmobile.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medical_reports",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class MedicalReportEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val extractionRunId: String?,
    val reportTitle: String,
    val vendorName: String,
    val vendorLocation: String?,
    val reportDate: String,
    val collectedAt: Long?,
    val patientName: String,
    val patientDob: String?,
    val patientSex: String?
)
