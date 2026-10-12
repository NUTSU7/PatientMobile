package com.semanticsoft.patientmobile.domain.repository.common

import com.semanticsoft.patientmobile.domain.model.common.AuthResponse
import com.semanticsoft.patientmobile.domain.model.common.RegistrationRequest
import com.semanticsoft.patientmobile.domain.model.common.User
import com.semanticsoft.patientmobile.core.util.ApiResult

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<AuthResponse>
    suspend fun register(request: RegistrationRequest): ApiResult<AuthResponse>
    suspend fun refresh(): ApiResult<AuthResponse>
    suspend fun logout()
    suspend fun getCurrentUser(): ApiResult<User>
    suspend fun isLoggedIn(): Boolean
    suspend fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String): ApiResult<Unit>
    suspend fun deleteAccount(password: String, confirmation: String): ApiResult<Unit>
    suspend fun requestPasswordReset(email: String): ApiResult<Unit>
    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmNewPassword: String
    ): ApiResult<Unit>
}
