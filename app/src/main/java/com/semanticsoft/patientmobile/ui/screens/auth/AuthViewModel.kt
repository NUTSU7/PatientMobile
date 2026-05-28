package com.semanticsoft.patientmobile.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.User
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

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedOut : AuthState
    data class Authenticated(val displayName: String, val email: String) : AuthState
    data object SessionExpired : AuthState
}

sealed interface AuthEvent {
    data object NavigateToLogin : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _state.update { AuthState.Loading }
            when (val result = authRepository.getCurrentUser()) {
                is ApiResult.Success -> {
                    _state.update { AuthState.Authenticated(
                        displayName = result.data.toDisplayName(),
                        email = result.data.email
                    )}
                }
                is ApiResult.AuthError -> {
                    _state.update { AuthState.SessionExpired }
                    _events.emit(AuthEvent.NavigateToLogin)
                }
                else -> {
                    _state.update { AuthState.SessionExpired }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {}
            _state.update { AuthState.LoggedOut }
            _events.emit(AuthEvent.NavigateToLogin)
        }
    }

    private fun User.toDisplayName(): String {
        val name = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        return name.ifBlank { email }
    }
}
