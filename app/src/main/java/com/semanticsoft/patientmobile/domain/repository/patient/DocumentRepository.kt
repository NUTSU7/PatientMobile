package com.semanticsoft.patientmobile.domain.repository.patient

import com.semanticsoft.patientmobile.domain.model.patient.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.patient.DocumentStats
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import com.semanticsoft.patientmobile.domain.model.patient.SharedLink
import com.semanticsoft.patientmobile.core.util.ApiResult
import java.io.File

interface DocumentRepository {
    suspend fun uploadDocument(file: File, force: Boolean = false): ApiResult<PatientDocument>
    suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>>
    suspend fun getDocuments(
        page: Int = 0,
        size: Int = 20,
        search: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        sortBy: String? = null,
        sortDir: String? = null
    ): ApiResult<List<PatientDocument>>
    suspend fun getDocumentById(id: String): ApiResult<PatientDocument>
    suspend fun downloadDocument(id: String): ApiResult<File>
    suspend fun downloadDocumentFile(documentId: String): ApiResult<File>
    suspend fun getDocumentExplanation(documentId: String): ApiResult<String>
    suspend fun renameDocument(id: String, newName: String): ApiResult<PatientDocument>
    suspend fun deleteDocument(id: String): ApiResult<Unit>
    suspend fun bulkDelete(documentIds: List<String>): ApiResult<Unit>
    suspend fun createShareLink(id: String): ApiResult<SharedLink>
    suspend fun getDocumentStats(): ApiResult<DocumentStats>
}
