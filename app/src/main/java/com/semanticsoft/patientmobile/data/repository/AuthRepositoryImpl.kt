package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.local.dao.UserDao
import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.local.db.entity.UserEntity
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.LoginRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.UserDto
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import java.time.LocalDate

class AuthRepositoryImpl(
    private val apiService: PatientApiService,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResponse {
        val response = apiService.login(LoginRequest(email = email, password = password))
        tokenManager.saveTokens(response.accessToken, response.refreshToken)

        val domainUser = response.user.toDomain()
        userDao.insert(domainUser.toEntity())

        return response.toDomain(domainUser)
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        val response = apiService.register(request)
        if (!response.isSuccessful) {
            throw IllegalStateException("Registration failed with HTTP ${response.code()}")
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
        tokenManager.saveTokens(response.accessToken, response.refreshToken)

        val domainUser = response.user.toDomain()
        userDao.insert(domainUser.toEntity())

        return response.toDomain(domainUser)
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
            tokenType = tokenType.name,
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
