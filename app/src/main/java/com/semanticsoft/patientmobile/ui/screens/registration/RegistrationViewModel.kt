package com.semanticsoft.patientmobile.ui.screens.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.util.PasswordValidator
import com.semanticsoft.patientmobile.util.exceptions.ApiException
import com.semanticsoft.patientmobile.util.exceptions.ApiValidationException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _events = MutableSharedFlow<RegistrationEvent>()
    val events: SharedFlow<RegistrationEvent> = _events.asSharedFlow()

    var state by mutableStateOf(RegistrationUiState())
        private set

    init {
        state = state.copy(
            name = savedStateHandle[KEY_NAME] ?: "",
            email = savedStateHandle[KEY_EMAIL] ?: "",
            password = savedStateHandle[KEY_PASSWORD] ?: "",
            confirmPassword = savedStateHandle[KEY_CONFIRM_PASSWORD] ?: ""
        )
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value, fieldErrors = state.fieldErrors - "name", errorMessage = null)
        savedStateHandle[KEY_NAME] = value
    }

    fun onEmailChange(value: String) {
        state = state.copy(email = value, fieldErrors = state.fieldErrors - "email", errorMessage = null)
        savedStateHandle[KEY_EMAIL] = value
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, fieldErrors = state.fieldErrors - "password", errorMessage = null)
        savedStateHandle[KEY_PASSWORD] = value
    }

    fun onConfirmPasswordChange(value: String) {
        state = state.copy(confirmPassword = value, fieldErrors = state.fieldErrors - "confirmPassword", errorMessage = null)
        savedStateHandle[KEY_CONFIRM_PASSWORD] = value
    }

    fun register() {
        if (state.isLoading) return

        val localErrors = validateInputs(state)
        if (localErrors.isNotEmpty()) {
            // Validation for incomplete/invalid local fields is handled in UI; do not call API.
            state = state.copy(fieldErrors = localErrors, errorMessage = null)
            return
        }

        val nameParts = state.name.trim().split(" ").filter { it.isNotBlank() }
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else firstName

        val request = RegisterRequest(
            email = state.email.trim(),
            password = state.password,
            confirmPassword = state.confirmPassword,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = "1990-01-01"
        )

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap())

            runCatching {
                authRepository.register(request)
            }.onSuccess {
                state = state.copy(isLoading = false, errorMessage = null)
                _events.emit(RegistrationEvent.RegistrationSuccess)
            }.onFailure { throwable ->
                val apiValidation = throwable as? ApiValidationException
                val fieldErrors = apiValidation?.fieldErrors?.associate { it.field to it.message }.orEmpty()
                val message = when {
                    apiValidation != null -> apiValidation.message ?: "Date invalide."
                    throwable is ApiException -> throwable.message ?: "Înregistrarea a eșuat."
                    else -> throwable.message ?: "Înregistrarea a eșuat."
                }

                // Clear password fields on error
                state = state.copy(
                    isLoading = false,
                    errorMessage = message,
                    fieldErrors = fieldErrors,
                    password = "",
                    confirmPassword = ""
                )
                savedStateHandle[KEY_PASSWORD] = ""
                savedStateHandle[KEY_CONFIRM_PASSWORD] = ""
                _events.emit(RegistrationEvent.RegistrationFailure(message))
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

    companion object {
        private const val KEY_NAME = "registration_name"
        private const val KEY_EMAIL = "registration_email"
        private const val KEY_PASSWORD = "registration_password"
        private const val KEY_CONFIRM_PASSWORD = "registration_confirm_password"
    }
}
