package com.semanticsoft.patientmobile.di

import com.semanticsoft.patientmobile.data.local.session.TokenManager
import com.semanticsoft.patientmobile.data.remote.api.AuthApiService
import com.semanticsoft.patientmobile.data.remote.api.SharedApiService
import com.semanticsoft.patientmobile.data.repository.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.data.repository.AuthRepositoryImpl
import com.semanticsoft.patientmobile.data.repository.NetworkStateProvider
import com.semanticsoft.patientmobile.data.repository.shared.GlobalSyncManagerImpl
import com.semanticsoft.patientmobile.data.repository.shared.ProcessingCoordinatorImpl
import com.semanticsoft.patientmobile.data.repository.shared.SharedLinkRepositoryImpl
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.ProcessingCoordinator
import com.semanticsoft.patientmobile.domain.repository.SharedLinkRepository
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
