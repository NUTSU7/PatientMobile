package com.semanticsoft.patientmobile.domain.usecase.common

import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.model.common.AuthResponse
import com.semanticsoft.patientmobile.domain.model.common.RegistrationRequest
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import javax.inject.Inject

class RegisterPatientUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): ApiResult<AuthResponse> {
        val nameParts = name.trim().split(" ").filter { it.isNotBlank() }
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else firstName

        return authRepository.register(
            RegistrationRequest(
                email = email.trim(),
                password = password,
                confirmPassword = confirmPassword,
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = DEFAULT_DATE_OF_BIRTH
            )
        )
    }

    private companion object {
        const val DEFAULT_DATE_OF_BIRTH = "1990-01-01"
    }
}
