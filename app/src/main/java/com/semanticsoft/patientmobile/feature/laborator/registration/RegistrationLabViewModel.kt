package com.semanticsoft.patientmobile.feature.laborator.registration

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegistrationLabUiState(
    val labName: String = "",
    val country: String = "",
    val cui: String = "",
    val contactName: String = "",
    val role: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class RegistrationLabEvent {
    data object RegistrationSuccess : RegistrationLabEvent()
    data class RegistrationFailure(val message: String) : RegistrationLabEvent()
}

@HiltViewModel
class RegistrationLabViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableSharedFlow<RegistrationLabEvent>()
    val events: SharedFlow<RegistrationLabEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(RegistrationLabUiState())
    val state: StateFlow<RegistrationLabUiState> = _state.asStateFlow()

    fun onLabNameChange(value: String) {
        _state.update { it.copy(labName = value, errorMessage = null) }
    }

    fun onCountryChange(value: String) {
        _state.update { it.copy(country = value, errorMessage = null) }
    }

    fun onCuiChange(value: String) {
        _state.update { it.copy(cui = value, errorMessage = null) }
    }

    fun onContactNameChange(value: String) {
        _state.update { it.copy(contactName = value, errorMessage = null) }
    }

    fun onRoleChange(value: String) {
        _state.update { it.copy(role = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _state.update { it.copy(phone = value, errorMessage = null) }
    }

    fun onMessageChange(value: String) {
        _state.update { it.copy(message = value, errorMessage = null) }
    }

    fun onRegisterClick() {
        // TODO: wire to backend partner registration when endpoint is available.
    }
}