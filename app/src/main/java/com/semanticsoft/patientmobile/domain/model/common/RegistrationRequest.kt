package com.semanticsoft.patientmobile.domain.model.common

data class RegistrationRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String
)
