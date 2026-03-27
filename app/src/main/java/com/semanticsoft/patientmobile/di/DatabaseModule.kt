package com.semanticsoft.patientmobile.di

import android.content.Context
import com.semanticsoft.patientmobile.data.local.dao.AuditLogDao
import com.semanticsoft.patientmobile.data.local.dao.DocumentDao
import com.semanticsoft.patientmobile.data.local.dao.MedicalResultDao
import com.semanticsoft.patientmobile.data.local.dao.UserDao
import com.semanticsoft.patientmobile.data.local.db.PatientDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PatientDatabase {
        return PatientDatabase.getInstance(context)
    }

    @Provides
    fun provideUserDao(db: PatientDatabase): UserDao = db.userDao()

    @Provides
    fun provideDocumentDao(db: PatientDatabase): DocumentDao = db.documentDao()

    @Provides
    fun provideMedicalResultDao(db: PatientDatabase): MedicalResultDao = db.medicalResultDao()

    @Provides
    fun provideAuditLogDao(db: PatientDatabase): AuditLogDao = db.auditLogDao()
}
