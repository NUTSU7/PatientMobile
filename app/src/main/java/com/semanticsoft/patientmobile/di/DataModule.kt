package com.semanticsoft.patientmobile.di

import android.content.Context
import com.semanticsoft.patientmobile.core.datastore.TokenManager
import com.semanticsoft.patientmobile.core.network.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.core.network.NetworkStateProvider
import com.semanticsoft.patientmobile.core.network.api.AuthApiService
import com.semanticsoft.patientmobile.core.network.api.PatientApiService
import com.semanticsoft.patientmobile.core.network.api.SharedApiService
import com.semanticsoft.patientmobile.data.repository.common.AuthRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.common.GlobalSyncManagerImpl
import com.semanticsoft.patientmobile.data.repository.patient.DashboardRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.patient.DocumentRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.patient.MedicalHistoryRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.patient.MedicalResultRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.patient.OcrRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.patient.ProcessingCoordinatorImpl
import com.semanticsoft.patientmobile.data.repository.patient.SharedLinkRepositoryImpl
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.common.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalHistoryRepository
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.patient.OcrRepository
import com.semanticsoft.patientmobile.domain.repository.patient.ProcessingCoordinator
import com.semanticsoft.patientmobile.domain.repository.patient.SharedLinkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideNetworkStateProvider(): NetworkStateProvider = AlwaysOnlineStateProvider

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: AuthApiService,
        tokenManager: TokenManager
    ): AuthRepository = AuthRepositoryImpl(apiService, tokenManager)

    @Provides
    @Singleton
    fun provideSharedLinkRepository(
        apiService: SharedApiService,
        networkStateProvider: NetworkStateProvider
    ): SharedLinkRepository = SharedLinkRepositoryImpl(apiService, networkStateProvider)

    @Provides
    @Singleton
    fun provideGlobalSyncManager(
        impl: GlobalSyncManagerImpl
    ): GlobalSyncManager = impl

    @Provides
    @Singleton
    fun provideProcessingCoordinator(
        impl: ProcessingCoordinatorImpl
    ): ProcessingCoordinator = impl

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
}
