package com.semanticsoft.patientmobile.data.repository.patient

import com.semanticsoft.patientmobile.core.network.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.core.network.NetworkStateProvider
import com.semanticsoft.patientmobile.core.network.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.core.network.api.SharedApiService
import com.semanticsoft.patientmobile.data.mapper.patient.toDomain
import com.semanticsoft.patientmobile.domain.model.patient.SharedLink
import com.semanticsoft.patientmobile.domain.repository.patient.SharedLinkRepository
import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.core.util.map

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
