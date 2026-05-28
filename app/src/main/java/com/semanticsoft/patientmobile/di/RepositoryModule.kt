package com.semanticsoft.patientmobile.di

import android.content.Context
import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.repository.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.data.repository.AuthRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.DashboardRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.DocumentRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.GlobalSyncManagerImpl
import com.semanticsoft.patientmobile.data.repository.MedicalHistoryRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.MedicalResultRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.NetworkStateProvider
import com.semanticsoft.patientmobile.data.repository.OcrRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.SharedLinkRepositoryImpl
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalHistoryRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.OcrRepository
import com.semanticsoft.patientmobile.domain.repository.SharedLinkRepository
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
        tokenManager: TokenManager
    ): AuthRepository = AuthRepositoryImpl(apiService, tokenManager)

    @Provides
    @Singleton
    fun provideDocumentRepository(
        @ApplicationContext context: Context,
        apiService: PatientApiService,
        networkStateProvider: NetworkStateProvider
    ): DocumentRepository = DocumentRepositoryImpl(context, apiService, networkStateProvider)

    @Provides
    @Singleton
    fun provideMedicalResultRepository(
        apiService: PatientApiService,
        networkStateProvider: NetworkStateProvider
    ): MedicalResultRepository = MedicalResultRepositoryImpl(apiService, networkStateProvider)

    @Provides
    @Singleton
    fun provideOcrRepository(
        apiService: PatientApiService,
        networkStateProvider: NetworkStateProvider
    ): OcrRepository = OcrRepositoryImpl(apiService, networkStateProvider)

    @Provides
    @Singleton
    fun provideDashboardRepository(
        apiService: PatientApiService,
        networkStateProvider: NetworkStateProvider
    ): DashboardRepository = DashboardRepositoryImpl(apiService, networkStateProvider)

    @Provides
    @Singleton
    fun provideMedicalHistoryRepository(
        apiService: PatientApiService,
        networkStateProvider: NetworkStateProvider
    ): MedicalHistoryRepository = MedicalHistoryRepositoryImpl(apiService, networkStateProvider)

    @Provides
    @Singleton
    fun provideSharedLinkRepository(
        apiService: PatientApiService,
        networkStateProvider: NetworkStateProvider
    ): SharedLinkRepository = SharedLinkRepositoryImpl(apiService, networkStateProvider)

    @Provides
    @Singleton
    fun provideGlobalSyncManager(
        impl: GlobalSyncManagerImpl
    ): GlobalSyncManager = impl
}