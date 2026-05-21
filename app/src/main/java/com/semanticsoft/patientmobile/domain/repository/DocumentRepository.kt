package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.SharedLink
import com.semanticsoft.patientmobile.util.ApiResult
import java.io.File

interface DocumentRepository {
    suspend fun uploadDocument(file: File, force: Boolean = false): ApiResult<PatientDocument>
    suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>>
    suspend fun getDocuments(
        page: Int = 0,
        size: Int = 20,
        search: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): ApiResult<List<PatientDocument>>
    suspend fun getDocumentById(id: String): ApiResult<PatientDocument>
    suspend fun downloadDocument(id: String): ApiResult<File>
    suspend fun renameDocument(id: String, newName: String): ApiResult<PatientDocument>
    suspend fun deleteDocument(id: String): ApiResult<Unit>
    suspend fun bulkDelete(documentIds: List<String>): ApiResult<Unit>
    suspend fun createShareLink(id: String): ApiResult<SharedLink>
}
