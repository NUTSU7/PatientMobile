package com.semanticsoft.patientmobile.feature.auth.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.core.ui.components.computePasswordCriteria
import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.usecase.common.RequestPasswordResetUseCase
import com.semanticsoft.patientmobile.domain.usecase.common.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    REQUEST,
    CONFIRM,
    SET_NEW,
    ERROR
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.REQUEST,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resendCooldownSeconds: Int = 0
)

sealed class ForgotPasswordEvent {
    data object PasswordChanged : ForgotPasswordEvent()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    private var cooldownJob: Job? = null

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value.take(MAX_EMAIL_LENGTH), errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        _state.update { it.copy(newPassword = value.take(MAX_PASSWORD_LENGTH), errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update { it.copy(confirmPassword = value.take(MAX_PASSWORD_LENGTH), errorMessage = null) }
    }

    fun onRequestSubmit() {
        val current = _state.value
        if (current.isLoading || current.email.isBlank() || !current.email.contains('@')) return
        requestResetCode(onSuccessStep = null)
    }

    fun onResendClick() {
        val current = _state.value
        if (current.isLoading || current.resendCooldownSeconds > 0) return
        requestResetCode(onSuccessStep = ForgotPasswordStep.CONFIRM)
    }

    fun onRequestNewCode() {
        val current = _state.value
        if (current.isLoading || current.resendCooldownSeconds > 0) return
        requestResetCode(onSuccessStep = ForgotPasswordStep.CONFIRM)
    }

    fun onCodeChange(value: String) {
        _state.update { it.copy(code = value.take(CODE_LENGTH)) }
        if (value.length == CODE_LENGTH) {
            _state.update { it.copy(step = ForgotPasswordStep.SET_NEW, errorMessage = null) }
        }
    }

    fun onSubmitNewPassword() {
        val current = _state.value
        if (current.isLoading) return

        val criteria = computePasswordCriteria(current.newPassword, current.email)
        val allMet = listOf(
            criteria.hasLength,
            criteria.hasUppercase,
            criteria.hasSpecial,
            criteria.hasDigit,
            criteria.notCommon,
            criteria.notPersonal
        ).all { it }
        if (!allMet || current.confirmPassword.isEmpty() || current.newPassword != current.confirmPassword) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = resetPasswordUseCase(
                email = current.email,
                code = current.code,
                newPassword = current.newPassword,
                confirmNewPassword = current.confirmPassword
            )) {
                is ApiResult.Success -> {
                    cooldownJob?.cancel()
                    _state.update { it.copy(isLoading = false, resendCooldownSeconds = 0) }
                    _events.emit(ForgotPasswordEvent.PasswordChanged)
                }
                is ApiResult.AuthError -> {
                    _state.update { it.copy(isLoading = false, step = ForgotPasswordStep.ERROR) }
                }
                is ApiResult.HttpError -> {
                    val message = when (result.code) {
                        429 -> {
                            startResendCooldown()
                            TOO_MANY_ATTEMPTS_MESSAGE
                        }
                        else -> result.message.ifBlank { "Eroare server (${result.code})." }
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                }
                is ApiResult.NetworkError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = NETWORK_ERROR_MESSAGE) }
                }
            }
        }
    }

    private fun requestResetCode(onSuccessStep: ForgotPasswordStep?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = requestPasswordResetUseCase(_state.value.email)) {
                is ApiResult.Success -> {
                    startResendCooldown()
                    _state.update {
                        it.copy(
                            step = onSuccessStep ?: ForgotPasswordStep.CONFIRM,
                            code = "",
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is ApiResult.AuthError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = GENERIC_ERROR_MESSAGE) }
                }
                is ApiResult.HttpError -> {
                    val message = when (result.code) {
                        429 -> {
                            startResendCooldown()
                            TOO_MANY_ATTEMPTS_MESSAGE
                        }
                        else -> result.message.ifBlank { "Eroare server (${result.code})." }
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                }
                is ApiResult.NetworkError -> {
                    _state.update { it.copy(isLoading = false, errorMessage = NETWORK_ERROR_MESSAGE) }
                }
            }
        }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var remaining = RESEND_COOLDOWN_SECONDS
            _state.update { it.copy(resendCooldownSeconds = remaining) }
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.update { it.copy(resendCooldownSeconds = remaining) }
            }
        }
    }

    private companion object {
        const val CODE_LENGTH = 6
        const val MAX_EMAIL_LENGTH = 254
        const val MAX_PASSWORD_LENGTH = 128
        const val RESEND_COOLDOWN_SECONDS = 120

        const val NETWORK_ERROR_MESSAGE = "Nu există conexiune la internet."
        const val TOO_MANY_ATTEMPTS_MESSAGE = "Prea multe încercări. Încearcă din nou în câteva minute."
        const val GENERIC_ERROR_MESSAGE = "Ceva nu a mers bine. Încearcă din nou."
    }
}