package com.semanticsoft.patientmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.semanticsoft.patientmobile.data.local.db.entity.MedicalReportEntity

@Dao
interface MedicalReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: MedicalReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<MedicalReportEntity>)

    @Query("SELECT * FROM medical_reports WHERE documentId = :documentId ORDER BY reportDate DESC")
    suspend fun getAllByDocumentId(documentId: String): List<MedicalReportEntity>

    @Query("DELETE FROM medical_reports WHERE documentId = :documentId")
    suspend fun deleteByDocumentId(documentId: String)
}
