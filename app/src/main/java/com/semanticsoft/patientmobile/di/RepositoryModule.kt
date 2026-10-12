package com.semanticsoft.patientmobile.di

import com.semanticsoft.patientmobile.core.datastore.TokenManager
import com.semanticsoft.patientmobile.core.network.api.AuthApiService
import com.semanticsoft.patientmobile.core.network.api.SharedApiService
import com.semanticsoft.patientmobile.core.network.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.data.repository.common.AuthRepositoryImpl
import com.semanticsoft.patientmobile.core.network.NetworkStateProvider
import com.semanticsoft.patientmobile.data.repository.common.GlobalSyncManagerImpl
import com.semanticsoft.patientmobile.data.repository.patient.ProcessingCoordinatorImpl
import com.semanticsoft.patientmobile.data.repository.patient.SharedLinkRepositoryImpl
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.common.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.ProcessingCoordinator
import com.semanticsoft.patientmobile.domain.repository.patient.SharedLinkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}
