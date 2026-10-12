package com.semanticsoft.patientmobile.data.repository.patient

import com.semanticsoft.patientmobile.core.network.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.core.network.NetworkStateProvider
import com.semanticsoft.patientmobile.core.network.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.core.network.api.PatientApiService
import com.semanticsoft.patientmobile.core.network.dto.patient.AiExplainRequest
import com.semanticsoft.patientmobile.data.mapper.patient.toDomain
import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.domain.repository.patient.DashboardRepository
import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.core.util.map

class DashboardRepositoryImpl(
    private val apiService: PatientApiService,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : DashboardRepository {

    override suspend fun getAiSummary(): ApiResult<AiSummary> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.getAiSummary() }.map { it.toDomain() }
    }

    override suspend fun regenerateAiSummary(): ApiResult<AiSummary> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.regenerateAiSummary() }.map { it.toDomain() }
    }

    override suspend fun requestExplanation(reportId: String): ApiResult<String> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.requestAiExplanation(AiExplainRequest(reportId)) }
            .map { it.explanation }
    }
}
