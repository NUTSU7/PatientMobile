package com.semanticsoft.patientmobile.ui.screens.registration

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class RegistrationUiState(
    val role: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class RegistrationViewModel : ViewModel() {
    var state by mutableStateOf(RegistrationUiState())
        private set

    fun onRoleChange(value: String) {
        state = state.copy(role = value)
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value)
    }

    fun onEmailChange(value: String) {
        state = state.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value)
    }

    fun onConfirmPasswordChange(value: String) {
        state = state.copy(confirmPassword = value)
    }

    fun register() {
        // API integration will be added in the next step.
    }
}
