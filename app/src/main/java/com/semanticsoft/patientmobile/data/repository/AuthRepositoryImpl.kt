package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.local.dao.UserDao
import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.local.db.entity.UserEntity
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.LoginRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.UserDto
import com.semanticsoft.patientmobile.domain.model.ApiErrorResponse
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.util.exceptions.ApiException
import com.semanticsoft.patientmobile.util.exceptions.ApiValidationException
import com.semanticsoft.patientmobile.util.exceptions.InvalidCredentialsException
import com.semanticsoft.patientmobile.util.exceptions.RateLimitException
import com.google.gson.Gson
import java.time.LocalDate

class AuthRepositoryImpl(
    private val apiService: PatientApiService,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResponse {
        val response = apiService.login(LoginRequest(email = email, password = password))
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            val errorMessage = runCatching {
                Gson().fromJson(errorBody, ApiErrorResponse::class.java).message
            }.getOrNull() ?: "Login failed"

            throw when (response.code()) {
                401 -> InvalidCredentialsException(errorMessage)
                429 -> RateLimitException(errorMessage)
                else -> ApiException(errorMessage, response.code())
            }
        }

        val authResponse = response.body() ?: throw ApiException("Empty response body", 500)
        tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)

        val domainUser = authResponse.user.toDomain()
        userDao.insert(domainUser.toEntity())

        return authResponse.toDomain(domainUser)
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        val response = apiService.register(request)
        if (!response.isSuccessful) {
            val bodyText = response.errorBody()?.string().orEmpty()
            val parsedError = runCatching {
                Gson().fromJson(bodyText, ApiErrorResponse::class.java)
            }.getOrNull()

            if (parsedError != null && parsedError.fieldErrors.isNotEmpty()) {
                throw ApiValidationException(
                    message = parsedError.message,
                    fieldErrors = parsedError.fieldErrors
                )
            }

            throw ApiException(
                message = parsedError?.message ?: "Registration failed with HTTP ${response.code()}",
                code = response.code()
            )
        }

        val body = requireNotNull(response.body()) { "Registration response body is empty" }
        tokenManager.saveTokens(body.accessToken, body.refreshToken)

        val domainUser = body.user.toDomain()
        userDao.insert(domainUser.toEntity())

        return body.toDomain(domainUser)
    }

    override suspend fun refresh(): AuthResponse {
        val refreshToken = tokenManager.getRefreshToken()
            ?: throw IllegalStateException("Missing refresh token")

        val response = apiService.refresh(RefreshRequest(refreshToken = refreshToken))
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            val errorMessage = runCatching {
                Gson().fromJson(errorBody, ApiErrorResponse::class.java).message
            }.getOrNull() ?: "Token refresh failed"

            throw when (response.code()) {
                401 -> InvalidCredentialsException(errorMessage)
                429 -> RateLimitException(errorMessage)
                else -> ApiException(errorMessage, response.code())
            }
        }

        val authResponse = response.body() ?: throw ApiException("Empty response body", 500)
        tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)

        val domainUser = authResponse.user.toDomain()
        userDao.insert(domainUser.toEntity())

        return authResponse.toDomain(domainUser)
    }

    override suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken().orEmpty()
        runCatching { apiService.logout(refreshToken) }
        tokenManager.clearTokens()
        userDao.deleteUser()
    }

    override suspend fun getCurrentUser(): User {
        return runCatching {
            val remoteUser = apiService.getCurrentUser()
            userDao.insert(remoteUser.toEntity())
            remoteUser
        }.getOrElse {
            val localUser = userDao.getUser()
                ?: throw it
            localUser.toDomain()
        }
    }

    private fun com.semanticsoft.patientmobile.data.remote.api.dto.AuthResponse.toDomain(user: User): AuthResponse {
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer",
            expiresIn = expiresIn,
            refreshExpiresIn = refreshExpiresIn,
            user = user
        )
    }

    private fun UserDto.toDomain(): User {
        return User(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = LocalDate.parse(dateOfBirth)
        )
    }

    private fun User.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth.toString()
        )
    }

    private fun UserEntity.toDomain(): User {
        return User(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = LocalDate.parse(dateOfBirth)
        )
    }
}
