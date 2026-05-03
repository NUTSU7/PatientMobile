package com.semanticsoft.patientmobile.di

import android.content.Context
import com.semanticsoft.patientmobile.data.local.dao.AuditLogDao
import com.semanticsoft.patientmobile.data.local.dao.DocumentDao
import com.semanticsoft.patientmobile.data.local.dao.MedicalResultDao
import com.semanticsoft.patientmobile.data.local.dao.UserDao
import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.repository.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.data.repository.AuditRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.AuthRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.DocumentRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.MedicalResultRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.NetworkStateProvider
import com.semanticsoft.patientmobile.domain.repository.AuditRepository
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNetworkStateProvider(): NetworkStateProvider = AlwaysOnlineStateProvider

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: PatientApiService,
        userDao: UserDao,
        tokenManager: TokenManager
    ): AuthRepository = AuthRepositoryImpl(apiService, userDao, tokenManager)

    @Provides
    @Singleton
    fun provideDocumentRepository(
        @ApplicationContext context: Context,
        apiService: PatientApiService,
        documentDao: DocumentDao,
        networkStateProvider: NetworkStateProvider
    ): DocumentRepository = DocumentRepositoryImpl(context, apiService, documentDao, networkStateProvider)

    @Provides
    @Singleton
    fun provideMedicalResultRepository(
        apiService: PatientApiService,
        medicalResultDao: MedicalResultDao,
        networkStateProvider: NetworkStateProvider
    ): MedicalResultRepository = MedicalResultRepositoryImpl(apiService, medicalResultDao, networkStateProvider)

    @Provides
    @Singleton
    fun provideAuditRepository(
        auditLogDao: AuditLogDao,
        userDao: UserDao,
        networkStateProvider: NetworkStateProvider
    ): AuditRepository = AuditRepositoryImpl(auditLogDao, userDao, networkStateProvider)
}
