package com.semanticsoft.patientmobile.domain.usecase.common

import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.model.common.AuthResponse
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiResult<AuthResponse> =
        authRepository.login(email, password)
}
