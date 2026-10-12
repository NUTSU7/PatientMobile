package com.semanticsoft.patientmobile.data.repository.patient

import com.semanticsoft.patientmobile.data.repository.AlwaysOnlineStateProvider
import com.semanticsoft.patientmobile.data.repository.NetworkStateProvider
import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.AiExplainRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.AiSummaryResponse
import com.semanticsoft.patientmobile.domain.repository.patient.DashboardRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map

class DashboardRepositoryImpl(
    private val apiService: PatientApiService,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : DashboardRepository {

    override suspend fun getAiSummary(): ApiResult<AiSummaryResponse> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.getAiSummary() }
    }

    override suspend fun regenerateAiSummary(): ApiResult<AiSummaryResponse> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.regenerateAiSummary() }
    }

    override suspend fun requestExplanation(reportId: String): ApiResult<String> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.requestAiExplanation(AiExplainRequest(reportId)) }
            .map { it.explanation }
    }
}
