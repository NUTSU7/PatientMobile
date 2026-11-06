package com.estcomputer.patient.feature.patient.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estcomputer.patient.designsystem.util.PasswordValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

sealed class RegistrationEvent {
    data object RegistrationSuccess : RegistrationEvent()
    data class RegistrationFailure(val message: String) : RegistrationEvent()
}

class RegistrationViewModel : ViewModel() {

    private val _events = MutableSharedFlow<RegistrationEvent>()
    val events: SharedFlow<RegistrationEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(RegistrationUiState())
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, fieldErrors = it.fieldErrors - "name", errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, fieldErrors = it.fieldErrors - "email", errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, fieldErrors = it.fieldErrors - "password", errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update { it.copy(confirmPassword = value, fieldErrors = it.fieldErrors - "confirmPassword", errorMessage = null) }
    }

    fun register() {
        if (_state.value.isLoading) return

        val localErrors = validateInputs(_state.value)
        if (localErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = localErrors, errorMessage = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = false, errorMessage = null, fieldErrors = emptyMap()) }
            _events.emit(RegistrationEvent.RegistrationSuccess)
        }
    }

    private fun validateInputs(current: RegistrationUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (current.name.isBlank()) errors["name"] = "Numele este obligatoriu."
        if (current.email.isBlank()) errors["email"] = "Emailul este obligatoriu."

        when (val result = PasswordValidator.validate(
            password = current.password,
            email = current.email,
            firstName = current.name.substringBefore(" "),
            lastName = current.name.substringAfter(" ", "")
        )) {
            is PasswordValidator.Result.Error -> errors["password"] = translatePasswordMessage(result.message)
            PasswordValidator.Result.Success -> Unit
        }

        if (current.password != current.confirmPassword) {
            errors["confirmPassword"] = "Parolele nu coincid."
        }

        return errors
    }

    private fun translatePasswordMessage(message: String): String {
        return when (message) {
            "Password must be at least 8 characters long." -> "Parola trebuie să aibă cel puțin 8 caractere."
            "Password must not match personal information." -> "Parola nu trebuie să conțină informații personale."
            else -> message
        }
    }
}
