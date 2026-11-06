package com.semanticsoft.patientmobile.feature.auth

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

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedOut : AuthState
    data class Authenticated(
        val displayName: String,
        val email: String
    ) : AuthState
    data object SessionExpired : AuthState
}

sealed interface AuthEvent {
    data object NavigateToLogin : AuthEvent
}

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun checkSession() {
        // Default: no-op in mobile-first state
    }

    fun logout() {
        viewModelScope.launch {
            _state.update { AuthState.LoggedOut }
            _events.emit(AuthEvent.NavigateToLogin)
        }
    }
}
