package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.AiSummaryResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.normalizeTestName
import com.semanticsoft.patientmobile.data.remote.api.dto.toDomain
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.PaginatedMedicalResults
import com.semanticsoft.patientmobile.domain.model.ReportWithResults
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
        analysisGroup: String?,
        sortBy: String?,
        sortDir: String?
    ): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall {
            apiService.getAllResults(page, size, analysisGroup, sortBy, sortDir)
        }.map { response -> response.content.map { it.toDomain() } }
    }

    override suspend fun getAllResultsPaginated(
        page: Int,
        size: Int,
        analysisGroup: String?,
        sortBy: String?,
        sortDir: String?
    ): ApiResult<PaginatedMedicalResults> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall {
            apiService.getAllResults(page, size, analysisGroup, sortBy, sortDir)
        }.map { response ->
            PaginatedMedicalResults(
                items = response.content.map { it.toDomain() },
                hasNext = response.hasNext
            )
        }
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
                    observedAt = entry.observedAt?.let(::parseObservedAt)
                )
            }
        }
    }

    override suspend fun getReportResults(reportId: String): ApiResult<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getReportResults(reportId) }.map { dto ->
            dto.results.map { item ->
                MedicalResult(
                    id = item.id,
                    testDefinitionId = item.testDefinitionId,
                    documentId = dto.documentId,
                    reportId = dto.reportId,
                    originalTestName = item.originalTestName,
                    canonicalName = item.canonicalName.normalizeTestName(),
                    analysisGroup = item.analysisGroup,
                    valueNumeric = item.valueNumeric,
                    valueText = item.valueText,
                    unit = item.unit,
                    referenceLow = item.referenceLow,
                    referenceHigh = item.referenceHigh,
                    referenceText = item.referenceText,
                    abnormalFlag = item.abnormalFlag,
                    observedAt = item.observedAt?.let(::parseObservedAt)
                )
            }
        }
    }

    override suspend fun getReportDetails(reportId: String): ApiResult<ReportWithResults> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError

        return safeApiCall { apiService.getReportResults(reportId) }.map { dto ->
            ReportWithResults(
                reportId = dto.reportId,
                documentId = dto.documentId,
                observedAt = dto.observedAt?.let(::parseObservedAt),
                clinicalType = dto.clinicalType,
                clinicalSubtype = dto.clinicalSubtype,
                summary = dto.summary,
                requiresReview = dto.requiresReview,
                results = dto.results.map { item ->
                    MedicalResult(
                        id = item.id,
                        testDefinitionId = item.testDefinitionId,
                        documentId = dto.documentId,
                        reportId = dto.reportId,
                        originalTestName = item.originalTestName,
                        canonicalName = item.canonicalName.normalizeTestName(),
                        analysisGroup = item.analysisGroup,
                        valueNumeric = item.valueNumeric,
                        valueText = item.valueText,
                        unit = item.unit,
                        referenceLow = item.referenceLow,
                        referenceHigh = item.referenceHigh,
                        referenceText = item.referenceText,
                        abnormalFlag = item.abnormalFlag,
                        observedAt = item.observedAt?.let(::parseObservedAt)
                    )
                }
            )
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

    private fun parseObservedAt(text: String): LocalDate {
        return try {
            Instant.parse(text).atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (_: Exception) {
            LocalDate.parse(text)
        }
    }
}