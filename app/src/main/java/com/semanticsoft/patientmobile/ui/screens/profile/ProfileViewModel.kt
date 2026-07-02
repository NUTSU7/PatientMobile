package com.semanticsoft.patientmobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.util.PasswordValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.async
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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository,
    private val globalSyncManager: GlobalSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState(isLoading = true))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            globalSyncManager.syncEvents.collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val userDeferred = async { authRepository.getCurrentUser() }
            val statsDeferred = async { documentRepository.getDocumentStats() }

            val userResult = userDeferred.await()
            val statsResult = statsDeferred.await()

            var email = ""
            var totalAnalyses = 0
            var daysSinceLastAnalysis = 0

            if (userResult is ApiResult.Success) {
                email = userResult.data.email
            }

            if (statsResult is ApiResult.Success) {
                val stats = statsResult.data
                totalAnalyses = stats.totalCount
                stats.lastUploadedAt?.let { lastAt ->
                    daysSinceLastAnalysis = ChronoUnit.DAYS.between(lastAt, Instant.now()).toInt()
                }
            }

            _state.update {
                it.copy(
                    email = email,
                    totalAnalyses = totalAnalyses,
                    daysSinceLastAnalysis = daysSinceLastAnalysis,
                    isLoading = false
                )
            }
        }
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
                    _effects.emit(ProfileEffect.ShowSnackbar("Func\u021Bionalitate \u00EEn curs de dezvoltare"))
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
        viewModelScope.launch {
            if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                _state.update { it.copy(changePasswordError = "Toate c\u00E2mpurile sunt obligatorii") }
                return@launch
            }
            if (newPassword != confirmPassword) {
                _state.update { it.copy(changePasswordError = "Parolele noi nu coincid") }
                return@launch
            }

            val validation = PasswordValidator.validate(
                password = newPassword,
                email = _state.value.email,
                firstName = "",
                lastName = ""
            )
            if (validation is PasswordValidator.Result.Error) {
                val msg = when {
                    validation.message.contains("8 characters") ->
                        "Parola trebuie s\u0103 aib\u0103 cel pu\u021Bin 8 caractere"
                    validation.message.contains("72 UTF-8") ->
                        "Parola dep\u0103\u0219e\u0219te limita de 72 bytes"
                    validation.message.contains("personal information") ->
                        "Parola nu trebuie s\u0103 con\u021Bin\u0103 date personale"
                    validation.message.contains("too common") ->
                        "Parola este prea comun\u0103"
                    else -> validation.message
                }
                _state.update { it.copy(changePasswordError = msg) }
                return@launch
            }

            _state.update { it.copy(isLoading = true, changePasswordError = null) }
            when (val result = authRepository.changePassword(currentPassword, newPassword, confirmPassword)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, showChangePasswordDialog = false) }
                    _effects.emit(
                        ProfileEffect.ShowSnackbar("Parola a fost actualizat\u0103 cu succes")
                    )
                    _effects.emit(ProfileEffect.NavigateToLogin)
                }
                is ApiResult.HttpError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            changePasswordError = result.message.ifBlank {
                                "Parola actual\u0103 este incorect\u0103"
                            }
                        )
                    }
                }
                is ApiResult.NetworkError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            changePasswordError = "Eroare de re\u021Bea. Verific\u0103 conexiunea."
                        )
                    }
                }
                is ApiResult.AuthError -> {
                    _state.update { it.copy(isLoading = false) }
                    _effects.emit(ProfileEffect.NavigateToLogin)
                }
            }
        }
    }

    private fun deleteAccount(password: String, confirmation: String) {
        viewModelScope.launch {
            if (password.isBlank()) {
                _state.update { it.copy(deleteAccountError = "Parola este obligatorie") }
                return@launch
            }
            if (confirmation != "\u0218TERGE CONTUL") {
                _state.update { it.copy(deleteAccountError = "Confirmarea nu corespunde. Scrie \u201E\u0218TERGE CONTUL\u201D.") }
                return@launch
            }
            _state.update { it.copy(isLoading = true, deleteAccountError = null) }
            when (val result = authRepository.deleteAccount(password, confirmation)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, showDeleteAccountDialog = false) }
                    _effects.emit(ProfileEffect.NavigateToLogin)
                }
                is ApiResult.HttpError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deleteAccountError = result.message.ifBlank { "\u0218tergerea contului a e\u0219uat" }
                        )
                    }
                }
                is ApiResult.NetworkError -> {
                    _state.update {
                        it.copy(isLoading = false, deleteAccountError = "Eroare de re\u021Bea. Verific\u0103 conexiunea.")
                    }
                }
                is ApiResult.AuthError -> {
                    _state.update { it.copy(isLoading = false) }
                    _effects.emit(ProfileEffect.NavigateToLogin)
                }
            }
        }
    }

    private fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
            _effects.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
