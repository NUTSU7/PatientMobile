package com.semanticsoft.patientmobile.data.repository.common

import com.semanticsoft.patientmobile.core.datastore.TokenManager
import com.semanticsoft.patientmobile.core.network.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.core.network.api.AuthApiService
import com.semanticsoft.patientmobile.core.network.dto.auth.ChangePasswordRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.DeleteAccountRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.LoginRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.LogoutRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.RefreshRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.RegisterRequest
import com.semanticsoft.patientmobile.data.mapper.common.toDomain
import com.semanticsoft.patientmobile.domain.model.common.AuthResponse as DomainAuthResponse
import com.semanticsoft.patientmobile.domain.model.common.RegistrationRequest
import com.semanticsoft.patientmobile.domain.model.common.User
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.core.util.map

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<DomainAuthResponse> {
        return safeApiCall {
            apiService.login(LoginRequest(email = email, password = password))
        }.map { response ->
            runCatching {
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
            }.onFailure {
                throw RuntimeException("[saveTokens] ${it.message}", it)
            }

            val domainAuth = runCatching {
                response.toDomain()
            }.onFailure {
                throw RuntimeException("[toDomain] ${it.message}", it)
            }.getOrThrow()

            runCatching {
                tokenManager.saveUser(domainAuth.user)
            }.onFailure {
                throw RuntimeException("[saveUser] ${it.message}", it)
            }

            domainAuth
        }
    }

    override suspend fun register(request: RegistrationRequest): ApiResult<DomainAuthResponse> {
        return safeApiCall {
            apiService.register(
                RegisterRequest(
                    email = request.email,
                    password = request.password,
                    confirmPassword = request.confirmPassword,
                    firstName = request.firstName,
                    lastName = request.lastName,
                    dateOfBirth = request.dateOfBirth
                )
            )
        }.map { response ->
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            val domainAuth = response.toDomain()
            tokenManager.saveUser(domainAuth.user)
            domainAuth
        }
    }

    override suspend fun refresh(): ApiResult<DomainAuthResponse> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return ApiResult.AuthError

        return safeApiCall {
            apiService.refresh(RefreshRequest(refreshToken = refreshToken))
        }.map { response ->
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            val domainAuth = response.toDomain()
            tokenManager.saveUser(domainAuth.user)
            domainAuth
        }.also { result ->
            if (result is ApiResult.AuthError) {
                tokenManager.clearTokens()
                tokenManager.clearUser()
            }
        }
    }

    override suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            safeApiCall { apiService.logout(LogoutRequest(refreshToken = refreshToken)) }
        }
        tokenManager.clearTokens()
        tokenManager.clearUser()
    }

    override suspend fun getCurrentUser(): ApiResult<User> {
        val result = safeApiCall { apiService.getCurrentUser() }.map { it.toDomain() }
        return when (result) {
            is ApiResult.Success -> {
                tokenManager.saveUser(result.data)
                result
            }
            is ApiResult.NetworkError, is ApiResult.AuthError -> {
                val localUser = tokenManager.getUser()
                if (localUser != null) {
                    ApiResult.Success(localUser)
                } else {
                    result
                }
            }
            else -> result
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.getAccessToken() != null
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): ApiResult<Unit> {
        return safeApiCall {
            apiService.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmNewPassword = confirmNewPassword
                )
            )
        }.map { }
    }

    override suspend fun deleteAccount(password: String, confirmation: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.deleteAccount(DeleteAccountRequest(password = password, confirmation = confirmation))
        }.also { result ->
            if (result is ApiResult.Success) {
                tokenManager.clearTokens()
                tokenManager.clearUser()
            }
        }.map { }
    }
}
