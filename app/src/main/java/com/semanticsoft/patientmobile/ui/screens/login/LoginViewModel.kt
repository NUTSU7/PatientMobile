package com.semanticsoft.patientmobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.util.ApiResult
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

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

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
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.login(currentEmail.trim(), currentPassword)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, errorMessage = null) }
                    _events.emit(LoginEvent.LoginSuccess)
                }

                is ApiResult.AuthError -> {
                    val message = "Email sau parolă incorecte."
                    _state.update { it.copy(isLoading = false, errorMessage = message, password = "") }
                    _events.emit(LoginEvent.LoginFailure(message))
                }

                is ApiResult.HttpError -> {
                    val message = when (result.code) {
                        401, 403 -> "Email sau parolă incorecte."
                        429 -> "Prea multe încercări. Încearcă din nou în câteva minute."
                        else -> result.message.ifBlank { "Eroare server (${result.code})." }
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = message, password = "") }
                    _events.emit(LoginEvent.LoginFailure(message))
                }

                is ApiResult.NetworkError -> {
                    val message = "Nu există conexiune la internet."
                    _state.update { it.copy(isLoading = false, errorMessage = message, password = "") }
                    _events.emit(LoginEvent.LoginFailure(message))
                }
            }
        }
    }
}