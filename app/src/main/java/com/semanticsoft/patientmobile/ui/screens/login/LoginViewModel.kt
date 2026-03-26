package com.semanticsoft.patientmobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class LoginUiState(
    val email: String = "",
    val password: String = ""
)

class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        state = state.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value)
    }

    fun login() {
        // API integration will be added in the next step.
    }
}
