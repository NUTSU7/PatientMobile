package com.estcomputer.patient.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medical_results",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicalReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["reportId"])
    ]
)
data class MedicalResultEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val reportId: String,
    val analysisType: String,
    val testName: String,
    val value: String,
    val unit: String,
    val referenceRange: String,
    val reportDate: String
)
