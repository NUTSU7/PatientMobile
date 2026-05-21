package com.semanticsoft.patientmobile.ui.screens.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.PasswordValidator
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

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

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

        val nameParts = _state.value.name.trim().split(" ").filter { it.isNotBlank() }
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else firstName

        val request = RegisterRequest(
            email = _state.value.email.trim(),
            password = _state.value.password,
            confirmPassword = _state.value.confirmPassword,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = "1990-01-01"
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap()) }

            when (val result = authRepository.register(request)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, errorMessage = null) }
                    _events.emit(RegistrationEvent.RegistrationSuccess)
                }

                is ApiResult.HttpError -> {
                    val message = when (result.code) {
                        409 -> "Un cont cu acest email există deja."
                        422 -> "Date invalide. Verifică câmpurile."
                        429 -> "Prea multe încercări. Încearcă din nou în câteva minute."
                        else -> result.message.ifBlank { "Înregistrarea a eșuat (${result.code})." }
                    }
                    _state.update { it.copy(isLoading = false, errorMessage = message, password = "", confirmPassword = "") }
                    _events.emit(RegistrationEvent.RegistrationFailure(message))
                }

                is ApiResult.NetworkError -> {
                    val message = "Nu există conexiune la internet."
                    _state.update { it.copy(isLoading = false, errorMessage = message, password = "", confirmPassword = "") }
                    _events.emit(RegistrationEvent.RegistrationFailure(message))
                }

                is ApiResult.AuthError -> {
                    val message = "Autentificarea a eșuat. Reîncearcă."
                    _state.update { it.copy(isLoading = false, errorMessage = message, password = "", confirmPassword = "") }
                    _events.emit(RegistrationEvent.RegistrationFailure(message))
                }
            }
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
            "Password must be at least 15 characters long." -> "Parola trebuie să aibă cel puțin 15 caractere."
            "Password must not match personal information." -> "Parola nu trebuie să conțină informații personale."
            else -> message
        }
    }
}