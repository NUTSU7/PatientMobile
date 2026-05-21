package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.AiSummaryResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.normalizeTestName
import com.semanticsoft.patientmobile.data.remote.api.dto.toDomain
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map
import java.time.LocalDate

class MedicalResultRepositoryImpl(
    private val apiService: PatientApiService,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : MedicalResultRepository {

    override suspend fun getByDocumentId(documentId: String): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall {
            apiService.getMedicalResultsByDocument(documentId)
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getLatestReport(documentId: String): ApiResult<MedicalReport> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        val result = safeApiCall { apiService.getReportsByDocument(documentId) }
        return when (result) {
            is ApiResult.Success -> {
                val extractions = result.data
                if (extractions.isNotEmpty() && extractions.first().reports.isNotEmpty()) {
                    val firstReport = extractions.first().reports.first()
                    val domainReport = firstReport.toDomain().copy(
                        documentId = documentId,
                        extractionRunId = extractions.first().id
                    )
                    ApiResult.Success(domainReport)
                } else {
                    ApiResult.HttpError(404, "No reports found")
                }
            }
            else -> result.cast()
        }
    }

    override suspend fun getAllResults(
        page: Int,
        size: Int,
        analysisGroup: String?
    ): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall {
            apiService.getAllResults(page, size, analysisGroup)
        }.map { response -> response.content.map { it.toDomain() } }
    }

    override suspend fun getResultById(resultId: String): ApiResult<MedicalResult> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getResultById(resultId) }.map { it.toDomain() }
    }

    override suspend fun getResultHistory(resultId: String): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getResultHistory(resultId) }.map { entries ->
            entries.map { entry ->
                MedicalResult(
                    id = entry.id,
                    documentId = "",
                    originalTestName = entry.originalTestName,
                    canonicalName = entry.originalTestName.normalizeTestName(),
                    analysisGroup = "",
                    valueNumeric = entry.valueNumeric,
                    valueText = entry.valueText,
                    unit = entry.unit,
                    observedAt = entry.observedAt?.let(LocalDate::parse)
                )
            }
        }
    }

    override suspend fun getReportResults(reportId: String): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getResultById(reportId) }.map { dto ->
            listOf(dto.toDomain())
        }
    }

    override suspend fun getLatestResults(): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getLatestResults() }.map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getResultsHistory(testDefinitionId: String): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall {
            apiService.getResultsHistoryByTestDefinition(testDefinitionId)
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAiExplanation(reportId: String): ApiResult<String> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall {
            apiService.getAiExplanation(reportId)
        }.map { it.explanation }
    }

    override suspend fun getAiSummary(): ApiResult<AiSummaryResponse> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getAiSummary() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T, R> ApiResult<T>.cast(): ApiResult<R> = when (this) {
        is ApiResult.HttpError -> ApiResult.HttpError(code, message)
        is ApiResult.NetworkError -> ApiResult.NetworkError
        is ApiResult.AuthError -> ApiResult.AuthError
        is ApiResult.Success -> throw IllegalStateException("Cannot cast Success variant")
    }
}