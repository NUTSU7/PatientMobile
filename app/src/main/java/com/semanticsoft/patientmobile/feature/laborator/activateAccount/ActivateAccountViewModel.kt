package com.semanticsoft.patientmobile.feature.laborator.activateAccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.core.ui.components.PasswordCriteria
import com.semanticsoft.patientmobile.core.ui.components.computePasswordCriteria
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

data class PasswordCriterionUi(
    val label: String,
    val met: Boolean
)

data class ActivateAccountUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false
) {
    val criteria: List<PasswordCriterionUi>
        get() = buildCriteria(password)

    val canSubmit: Boolean
        get() = termsAccepted
}

private fun buildCriteria(password: String): List<PasswordCriterionUi> {
    val criteria = if (password.isEmpty()) {
        PasswordCriteria()
    } else {
        computePasswordCriteria(password)
    }
    return listOf(
        PasswordCriterionUi("Minim 8 caractere", criteria.hasLength),
        PasswordCriterionUi("Minim o literă majusculă", criteria.hasUppercase),
        PasswordCriterionUi("Minim un caracter special (ex: !, @, #, $, etc.)", criteria.hasSpecial),
        PasswordCriterionUi("Minim o cifră", criteria.hasDigit),
        PasswordCriterionUi("Parola nu este prea comună", criteria.notCommon),
        PasswordCriterionUi("Parola nu este bazată pe date personale", criteria.notPersonal)
    )
}

sealed interface ActivateAccountEvent {
    data object Activated : ActivateAccountEvent
}

@HiltViewModel
class ActivateAccountViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ActivateAccountUiState())
    val state: StateFlow<ActivateAccountUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ActivateAccountEvent>()
    val events: SharedFlow<ActivateAccountEvent> = _events.asSharedFlow()

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update { it.copy(confirmPassword = value) }
    }

    fun onTermsAcceptedChange(value: Boolean) {
        _state.update { it.copy(termsAccepted = value) }
    }

    fun onActivateClick() {
        if (!_state.value.canSubmit || _state.value.isLoading) return
        viewModelScope.launch {
            _events.emit(ActivateAccountEvent.Activated)
        }
    }
}
