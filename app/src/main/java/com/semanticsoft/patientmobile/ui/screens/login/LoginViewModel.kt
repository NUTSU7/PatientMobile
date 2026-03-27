package com.semanticsoft.patientmobile.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.util.exceptions.ApiException
import com.semanticsoft.patientmobile.util.exceptions.InvalidCredentialsException
import com.semanticsoft.patientmobile.util.exceptions.RateLimitException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    var state by mutableStateOf(LoginUiState())
        private set

    init {
        state = state.copy(
            email = savedStateHandle[KEY_EMAIL] ?: "",
            password = savedStateHandle[KEY_PASSWORD] ?: ""
        )
    }

    fun onEmailChange(value: String) {
        state = state.copy(email = value, errorMessage = null)
        savedStateHandle[KEY_EMAIL] = value
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, errorMessage = null)
        savedStateHandle[KEY_PASSWORD] = value
    }

    fun login() {
        if (state.isLoading) return

        if (state.email.isBlank() || state.password.isBlank()) {
            val message = "Completează emailul și parola."
            state = state.copy(errorMessage = message)
            viewModelScope.launch { _events.emit(LoginEvent.LoginFailure(message)) }
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            runCatching {
                authRepository.login(state.email.trim(), state.password)
            }.onSuccess {
                state = state.copy(isLoading = false, errorMessage = null)
                _events.emit(LoginEvent.LoginSuccess)
            }.onFailure { throwable ->
                val message = when (throwable) {
                    is InvalidCredentialsException -> "Email sau parolă incorecte."
                    is RateLimitException -> "Prea multe încercări. Încearcă din nou în câteva minute."
                    is ApiException -> throwable.message ?: "Eroare API. Încearcă din nou."
                    else -> throwable.message ?: "Autentificarea a eșuat."
                }

                state = state.copy(isLoading = false, errorMessage = message)
                _events.emit(LoginEvent.LoginFailure(message))
            }
        }
    }

    companion object {
        private const val KEY_EMAIL = "login_email"
        private const val KEY_PASSWORD = "login_password"
    }
}
