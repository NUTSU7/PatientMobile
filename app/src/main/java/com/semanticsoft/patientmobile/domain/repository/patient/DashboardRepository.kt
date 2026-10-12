package com.semanticsoft.patientmobile.domain.repository.patient

import com.semanticsoft.patientmobile.core.network.dto.patient.AiSummaryResponse
import com.semanticsoft.patientmobile.core.util.ApiResult

interface DashboardRepository {
    suspend fun getAiSummary(): ApiResult<AiSummaryResponse>
    suspend fun regenerateAiSummary(): ApiResult<AiSummaryResponse>
    suspend fun requestExplanation(reportId: String): ApiResult<String>
}
