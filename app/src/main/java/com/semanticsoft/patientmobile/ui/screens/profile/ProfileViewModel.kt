package com.semanticsoft.patientmobile.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.util.ApiResult
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
    val successMessage: String? = null,
    val showChangePasswordDialog: Boolean = false
)

sealed class ProfileEvent {
    data object OnUploadClicked : ProfileEvent()
    data object OnChangePasswordClicked : ProfileEvent()
    data object OnChangePasswordDismiss : ProfileEvent()
    data class OnChangePasswordSubmit(val oldPassword: String, val newPassword: String) : ProfileEvent()
    data object OnExportDataClicked : ProfileEvent()
    data object OnDeleteAccountClicked : ProfileEvent()
    data object OnLogoutClicked : ProfileEvent()
}

sealed class ProfileEffect {
    data class ShowSnackbar(val message: String) : ProfileEffect()
    data object NavigateToLogin : ProfileEffect()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState(isLoading = true))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnUploadClicked -> Unit
            ProfileEvent.OnChangePasswordClicked -> {
                _state.update { it.copy(showChangePasswordDialog = true) }
            }
            ProfileEvent.OnChangePasswordDismiss -> {
                _state.update { it.copy(showChangePasswordDialog = false) }
            }
            is ProfileEvent.OnChangePasswordSubmit -> {
                changePassword(event.oldPassword, event.newPassword)
            }
            ProfileEvent.OnExportDataClicked -> {
                viewModelScope.launch {
                    _effects.emit(ProfileEffect.ShowSnackbar("Func\u021Bionalitate \u00EEn curs de dezvoltare"))
                }
            }
            ProfileEvent.OnDeleteAccountClicked -> onDeleteAccount()
            ProfileEvent.OnLogoutClicked -> onLogout()
        }
    }

    private fun loadProfile() {
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

    private fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = authRepository.changePassword(oldPassword, newPassword)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showChangePasswordDialog = false,
                            successMessage = "Parola a fost schimbat\u0103 cu succes"
                        )
                    }
                }
                is ApiResult.HttpError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message.ifBlank { "Schimbarea parolei a e\u0219uat" }
                        )
                    }
                }
                is ApiResult.NetworkError -> {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "Eroare de re\u021Bea. Verific\u0103 conexiunea.")
                    }
                }
                is ApiResult.AuthError -> {
                    _state.update { it.copy(isLoading = false, showChangePasswordDialog = false) }
                    _effects.emit(ProfileEffect.NavigateToLogin)
                }
            }
        }
    }

    private fun onDeleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.deleteAccount()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _effects.emit(ProfileEffect.NavigateToLogin)
                }
                is ApiResult.HttpError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message.ifBlank { "\u0218tergerea contului a e\u0219uat" }
                        )
                    }
                }
                is ApiResult.NetworkError -> {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "Eroare de re\u021Bea. Verific\u0103 conexiunea.")
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
