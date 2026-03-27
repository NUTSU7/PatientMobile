package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.util.Resource
import java.io.File
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    suspend fun uploadDocument(file: File): PatientDocument
    fun getDocuments(): Flow<Resource<List<PatientDocument>>>
    fun getDocumentById(id: String): Flow<Resource<PatientDocument>>
    suspend fun downloadDocument(id: String): File
}
