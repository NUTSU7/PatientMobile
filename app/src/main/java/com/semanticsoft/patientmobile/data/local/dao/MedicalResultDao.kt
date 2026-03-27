package com.estcomputer.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.estcomputer.patient.data.local.db.entity.MedicalResultEntity

@Dao
interface MedicalResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: MedicalResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<MedicalResultEntity>)

    @Query("SELECT * FROM medical_results WHERE documentId = :documentId ORDER BY reportDate DESC")
    suspend fun getAllByDocumentId(documentId: String): List<MedicalResultEntity>

    @Query("DELETE FROM medical_results WHERE documentId = :documentId")
    suspend fun deleteByDocumentId(documentId: String)
}
