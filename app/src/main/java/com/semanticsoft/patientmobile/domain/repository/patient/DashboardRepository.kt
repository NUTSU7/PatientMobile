package com.semanticsoft.patientmobile.domain.repository.patient

import com.semanticsoft.patientmobile.data.remote.api.dto.patient.AiSummaryResponse
import com.semanticsoft.patientmobile.util.ApiResult

interface DashboardRepository {
    suspend fun getAiSummary(): ApiResult<AiSummaryResponse>
    suspend fun regenerateAiSummary(): ApiResult<AiSummaryResponse>
    suspend fun requestExplanation(reportId: String): ApiResult<String>
}
