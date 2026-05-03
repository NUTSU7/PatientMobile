package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.util.Resource

interface AuditRepository {
    suspend fun logAction(action: String, resourceType: String, status: String)
    suspend fun sync(): Resource<Unit>
}
