package com.semanticsoft.patientmobile.domain.repository.patient

import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.core.util.ApiResult

interface DashboardRepository {
    suspend fun getAiSummary(): ApiResult<AiSummary>
    suspend fun regenerateAiSummary(): ApiResult<AiSummary>
    suspend fun requestExplanation(reportId: String): ApiResult<String>
}
