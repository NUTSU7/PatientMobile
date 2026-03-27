package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun refresh(): AuthResponse
    suspend fun logout()
    suspend fun getCurrentUser(): User
}
