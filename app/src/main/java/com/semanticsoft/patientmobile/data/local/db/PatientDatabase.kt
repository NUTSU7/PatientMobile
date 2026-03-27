package com.estcomputer.patient.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.estcomputer.patient.data.local.dao.AuditLogDao
import com.estcomputer.patient.data.local.dao.DocumentDao
import com.estcomputer.patient.data.local.dao.MedicalReportDao
import com.estcomputer.patient.data.local.dao.MedicalResultDao
import com.estcomputer.patient.data.local.dao.UserDao
import com.estcomputer.patient.data.local.db.entity.AuditLogEntity
import com.estcomputer.patient.data.local.db.entity.DocumentEntity
import com.estcomputer.patient.data.local.db.entity.MedicalReportEntity
import com.estcomputer.patient.data.local.db.entity.MedicalResultEntity
import com.estcomputer.patient.data.local.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DocumentEntity::class,
        MedicalReportEntity::class,
        MedicalResultEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PatientDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun documentDao(): DocumentDao
    abstract fun medicalReportDao(): MedicalReportDao
    abstract fun medicalResultDao(): MedicalResultDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        private const val DATABASE_NAME = "patient_database"

        @Volatile
        private var INSTANCE: PatientDatabase? = null

        fun getInstance(context: Context): PatientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatientDatabase::class.java,
                    DATABASE_NAME
                )
                    // Migration strategy: destructive fallback until explicit v2 migration is added.
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
