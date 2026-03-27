package com.estcomputer.patient.domain.model

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshExpiresIn: Long,
    val user: User
)
