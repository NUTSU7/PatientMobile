package com.semanticsoft.patientmobile.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginEvent {
    data object LoginSuccess : LoginEvent()
    data class LoginFailure(val message: String) : LoginEvent()
}

class LoginViewModel : ViewModel() {

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        if (_state.value.isLoading) return

        val currentEmail = _state.value.email
        val currentPassword = _state.value.password
        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            return
        }

        viewModelScope.launch {
            _events.emit(LoginEvent.LoginSuccess)
        }
    }
}
