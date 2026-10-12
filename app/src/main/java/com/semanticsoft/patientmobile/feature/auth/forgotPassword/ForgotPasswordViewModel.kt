package com.semanticsoft.patientmobile.feature.auth.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.core.ui.components.computePasswordCriteria
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    val confirmPassword: String = ""
)

sealed class ForgotPasswordEvent {
    data class CodeSubmitted(val code: String) : ForgotPasswordEvent()
    data object ResendRequested : ForgotPasswordEvent()
    data object PasswordChanged : ForgotPasswordEvent()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value.take(MAX_EMAIL_LENGTH)) }
    }

    fun onRequestSubmit() {
        _state.update { current ->
            if (current.email.isNotBlank()) {
                current.copy(step = ForgotPasswordStep.CONFIRM)
            } else {
                current
            }
        }
    }

    fun onNewPasswordChange(value: String) {
        _state.update { it.copy(newPassword = value.take(MAX_PASSWORD_LENGTH)) }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update { it.copy(confirmPassword = value.take(MAX_PASSWORD_LENGTH)) }
    }

    fun onSubmitNewPassword() {
        val current = _state.value
        val criteria = computePasswordCriteria(current.newPassword, current.email)
        val allMet = listOf(
            criteria.hasLength,
            criteria.hasUppercase,
            criteria.hasSpecial,
            criteria.hasDigit,
            criteria.notCommon,
            criteria.notPersonal
        ).all { it }
        if (allMet && current.confirmPassword.isNotEmpty() && current.newPassword == current.confirmPassword) {
            viewModelScope.launch {
                _events.emit(ForgotPasswordEvent.PasswordChanged)
            }
        }
    }

    fun onCodeChange(value: String) {
        _state.update { it.copy(code = value) }
        if (value.length == CODE_LENGTH) {
            _state.update { it.copy(step = ForgotPasswordStep.SET_NEW) }
            viewModelScope.launch {
                _events.emit(ForgotPasswordEvent.CodeSubmitted(value))
            }
        }
    }

    fun onResendClick() {
        _state.update { it.copy(step = ForgotPasswordStep.ERROR) }
    }

    fun onRequestNewCode() {
        _state.update {
            it.copy(step = ForgotPasswordStep.CONFIRM, code = "")
        }
    }

    private companion object {
        const val CODE_LENGTH = 6
        const val MAX_EMAIL_LENGTH = 254
        const val MAX_PASSWORD_LENGTH = 128
    }
}
