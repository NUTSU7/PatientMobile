package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("tokenType") val tokenType: String? = null,
    @SerialName("expiresIn") val expiresIn: Long,
    @SerialName("refreshExpiresIn") val refreshExpiresIn: Long,
    @SerialName("user") val user: UserDto
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class RefreshResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("tokenType") val tokenType: String? = null,
    @SerialName("expiresIn") val expiresIn: Long,
    @SerialName("refreshExpiresIn") val refreshExpiresIn: Long,
    @SerialName("user") val user: UserDto
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("currentPassword") val currentPassword: String,
    @SerialName("newPassword") val newPassword: String,
    @SerialName("confirmNewPassword") val confirmNewPassword: String
)

@Serializable
data class DeleteAccountRequest(
    val password: String,
    val confirmation: String
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String? = null,
    val dateOfBirth: String? = null,
    val role: String = "PATIENT",
    val createdAt: String? = null
)
