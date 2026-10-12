package com.semanticsoft.patientmobile.domain.usecase.common

import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import javax.inject.Inject

class RequestPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): ApiResult<Unit> =
        authRepository.requestPasswordReset(email)
}