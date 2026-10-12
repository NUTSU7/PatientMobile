package com.semanticsoft.patientmobile.domain.repository.patient

import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.domain.model.patient.MedicalReport
import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.domain.model.patient.PaginatedMedicalResults
import com.semanticsoft.patientmobile.domain.model.patient.ReportWithResults
import com.semanticsoft.patientmobile.core.util.ApiResult

interface MedicalResultRepository {
    suspend fun getByDocumentId(documentId: String): ApiResult<List<MedicalResult>>
    suspend fun getLatestReport(documentId: String): ApiResult<MedicalReport>
    suspend fun getAllResults(
        page: Int = 0,
        size: Int = 20,
        analysisGroup: String? = null,
        sortBy: String? = null,
        sortDir: String? = null
    ): ApiResult<List<MedicalResult>>
    suspend fun getAllResultsPaginated(
        page: Int = 0,
        size: Int = 20,
        analysisGroup: String? = null,
        sortBy: String? = null,
        sortDir: String? = null
    ): ApiResult<PaginatedMedicalResults>
    suspend fun getResultById(resultId: String): ApiResult<MedicalResult>
    suspend fun getResultHistory(resultId: String): ApiResult<List<MedicalResult>>
    suspend fun getReportResults(reportId: String): ApiResult<List<MedicalResult>>
    suspend fun getReportDetails(reportId: String): ApiResult<ReportWithResults>
    suspend fun getLatestResults(): ApiResult<List<MedicalResult>>
    suspend fun getResultsHistory(testDefinitionId: String): ApiResult<List<MedicalResult>>
    suspend fun getAiExplanation(reportId: String): ApiResult<String>
    suspend fun getAiSummary(): ApiResult<AiSummary>
    suspend fun updateReport(
        reportId: String,
        observedAt: String?,
        displayTitle: String?,
        displaySubtitle: String?,
        sourceLaboratory: String?
    ): ApiResult<ReportWithResults>
}
