package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.remote.SafeApiCall.safeApiCall
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.LoginRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.LogoutRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.toDomain
import com.semanticsoft.patientmobile.domain.model.AuthResponse as DomainAuthResponse
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.map

class AuthRepositoryImpl(
    private val apiService: PatientApiService,
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

    override suspend fun register(request: RegisterRequest): ApiResult<DomainAuthResponse> {
        return safeApiCall { apiService.register(request) }.map { response ->
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
}
