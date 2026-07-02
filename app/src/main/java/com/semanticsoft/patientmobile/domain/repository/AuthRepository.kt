package com.semanticsoft.patientmobile.domain.repository

import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.util.ApiResult

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<AuthResponse>
    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse>
    suspend fun refresh(): ApiResult<AuthResponse>
    suspend fun logout()
    suspend fun getCurrentUser(): ApiResult<User>
    suspend fun isLoggedIn(): Boolean
    suspend fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String): ApiResult<Unit>
    suspend fun deleteAccount(password: String, confirmation: String): ApiResult<Unit>
}
