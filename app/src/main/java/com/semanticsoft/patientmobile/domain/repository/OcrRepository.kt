package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.domain.model.OcrExtraction
import com.semanticsoft.patientmobile.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface OcrRepository {
    suspend fun startExtraction(documentId: String): ApiResult<String>
    suspend fun getExtractionStatus(documentId: String, runId: String): ApiResult<OcrExtraction>
    suspend fun listExtractions(documentId: String): ApiResult<List<OcrExtraction>>
    fun pollOcrStatus(documentId: String, runId: String): Flow<ApiResult<OcrExtraction>>
}
