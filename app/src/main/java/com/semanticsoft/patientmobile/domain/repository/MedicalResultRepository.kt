package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.util.Resource
import kotlinx.coroutines.flow.Flow

interface MedicalResultRepository {
    fun getByDocumentId(docId: String): Flow<Resource<List<MedicalResult>>>
    suspend fun sync(docId: String): Resource<List<MedicalResult>>
}
