package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.ApiConstants
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.toDomain
import com.semanticsoft.patientmobile.domain.model.OcrExtraction
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.domain.repository.OcrRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OcrRepositoryImpl(
    private val apiService: PatientApiService,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : OcrRepository {

    override suspend fun startExtraction(documentId: String): ApiResult<String> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall { apiService.startExtraction(documentId) }.map { it.runId }
    }

    override suspend fun getExtractionStatus(
        documentId: String,
        runId: String
    ): ApiResult<OcrExtraction> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall {
            apiService.getExtractionStatus(documentId, runId)
        }.map { it.toDomain() }
    }

    override suspend fun listExtractions(
        documentId: String
    ): ApiResult<List<OcrExtraction>> {
        if (!networkStateProvider.isOnline()) return ApiResult.NetworkError
        return safeApiCall {
            apiService.listExtractions(documentId)
        }.map { list -> list.map { it.toDomain() } }
    }

    override fun pollOcrStatus(
        documentId: String,
        runId: String
    ): Flow<ApiResult<OcrExtraction>> = flow {
        var retries = 0
        while (retries < ApiConstants.OCR_POLL_MAX_RETRIES) {
            if (!networkStateProvider.isOnline()) {
                emit(ApiResult.NetworkError)
                return@flow
            }

            when (val result = safeApiCall {
                apiService.getExtractionStatus(documentId, runId)
            }) {
                is ApiResult.Success -> {
                    val extraction = result.data.toDomain()

                    when (extraction.status) {
                        OcrStatus.COMPLETED -> {
                            emit(ApiResult.Success(extraction))
                            return@flow
                        }
                        OcrStatus.FAILED -> {
                            emit(ApiResult.HttpError(422, "OCR processing failed"))
                            return@flow
                        }
                        else -> {
                            emit(ApiResult.Success(extraction))
                        }
                    }
                }
                is ApiResult.HttpError -> {
                    emit(result)
                    return@flow
                }
                is ApiResult.NetworkError -> {
                    emit(ApiResult.NetworkError)
                    return@flow
                }
                is ApiResult.AuthError -> {
                    emit(ApiResult.AuthError)
                    return@flow
                }
            }

            retries++
            delay(ApiConstants.OCR_POLL_DELAY_MS)
        }

        emit(ApiResult.HttpError(408, "OCR processing timed out"))
    }
}
