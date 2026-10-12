package com.semanticsoft.patientmobile.domain.usecase.common

import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
