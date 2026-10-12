package com.semanticsoft.patientmobile.data.repository.shared

import com.semanticsoft.patientmobile.data.repository.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.data.repository.NetworkStateProvider
import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.SharedApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.mappers.toDomain
import com.semanticsoft.patientmobile.domain.model.SharedLink
import com.semanticsoft.patientmobile.domain.repository.SharedLinkRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map

class SharedLinkRepositoryImpl(
    private val apiService: SharedApiService,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : SharedLinkRepository {

    override suspend fun consumeSharedLink(
        token: String,
        passphrase: String?
    ): ApiResult<SharedLink> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.consumeSharedLink(token, passphrase) }.map { it.toDomain() }
    }
}
