package com.semanticsoft.patientmobile.data.remote.api.dto

enum class TokenType {
    BEARER
}

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: TokenType,
    val expiresIn: Long,
    val refreshExpiresIn: Long,
    val user: UserDto
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String
)
