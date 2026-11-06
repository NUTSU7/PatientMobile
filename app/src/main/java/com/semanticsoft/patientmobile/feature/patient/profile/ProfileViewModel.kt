package com.semanticsoft.patientmobile.feature.patient.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.core.util.PasswordValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val email: String = "",
    val totalAnalyses: Int = 0,
    val daysSinceLastAnalysis: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val changePasswordError: String? = null,
    val showChangePasswordDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val deleteAccountError: String? = null
)

sealed class ProfileEvent {
    data object OnUploadClicked : ProfileEvent()
    data object OnChangePasswordClicked : ProfileEvent()
    data object OnChangePasswordDismiss : ProfileEvent()
    data class OnChangePasswordSubmit(
        val oldPassword: String,
        val newPassword: String,
        val confirmPassword: String
    ) : ProfileEvent()
    data object OnExportDataClicked : ProfileEvent()
    data object OnDeleteAccountClicked : ProfileEvent()
    data object OnDeleteAccountDismiss : ProfileEvent()
    data class OnDeleteAccountConfirm(
        val password: String,
        val confirmation: String
    ) : ProfileEvent()
    data object OnLogoutClicked : ProfileEvent()
}

sealed class ProfileEffect {
    data class ShowSnackbar(val message: String) : ProfileEffect()
    data object NavigateToLogin : ProfileEffect()
}

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    fun refresh() {
        // Default no-op
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnUploadClicked -> Unit
            ProfileEvent.OnChangePasswordClicked -> {
                _state.update {
                    it.copy(showChangePasswordDialog = true, changePasswordError = null)
                }
            }
            ProfileEvent.OnChangePasswordDismiss -> {
                _state.update {
                    it.copy(showChangePasswordDialog = false, changePasswordError = null)
                }
            }
            is ProfileEvent.OnChangePasswordSubmit -> {
                changePassword(event.oldPassword, event.newPassword, event.confirmPassword)
            }
            ProfileEvent.OnExportDataClicked -> {
                viewModelScope.launch {
                    _effects.emit(ProfileEffect.ShowSnackbar("Funcționalitate în curs de dezvoltare"))
                }
            }
            ProfileEvent.OnDeleteAccountClicked -> {
                _state.update {
                    it.copy(showDeleteAccountDialog = true, deleteAccountError = null)
                }
            }
            ProfileEvent.OnDeleteAccountDismiss -> {
                _state.update {
                    it.copy(showDeleteAccountDialog = false, deleteAccountError = null)
                }
            }
            is ProfileEvent.OnDeleteAccountConfirm -> {
                deleteAccount(event.password, event.confirmation)
            }
            ProfileEvent.OnLogoutClicked -> onLogout()
        }
    }

    private fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _state.update { it.copy(changePasswordError = "Toate câmpurile sunt obligatorii") }
            return
        }
        if (newPassword != confirmPassword) {
            _state.update { it.copy(changePasswordError = "Parolele noi nu coincid") }
            return
        }

        val validation = PasswordValidator.validate(
            password = newPassword,
            email = _state.value.email,
            firstName = "",
            lastName = ""
        )
        if (validation is PasswordValidator.Result.Error) {
            _state.update { it.copy(changePasswordError = validation.message) }
            return
        }

        _state.update { it.copy(showChangePasswordDialog = false) }
        viewModelScope.launch {
            _effects.emit(ProfileEffect.ShowSnackbar("Parola a fost actualizată cu succes"))
        }
    }

    private fun deleteAccount(password: String, confirmation: String) {
        if (password.isBlank()) {
            _state.update { it.copy(deleteAccountError = "Parola este obligatorie") }
            return
        }
        if (confirmation != "ȘTERGE CONTUL") {
            _state.update { it.copy(deleteAccountError = "Confirmarea nu corespunde. Scrie „ȘTERGE CONTUL”.") }
            return
        }
        _state.update { it.copy(showDeleteAccountDialog = false) }
        viewModelScope.launch {
            _effects.emit(ProfileEffect.NavigateToLogin)
        }
    }

    private fun onLogout() {
        viewModelScope.launch {
            _effects.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
