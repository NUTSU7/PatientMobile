package com.semanticsoft.patientmobile.domain.model

import java.time.LocalDate

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate
)
