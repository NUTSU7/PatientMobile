package com.semanticsoft.patientmobile.feature.laborator.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.common.UserRole
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.core.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LaboratorProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val role: UserRole = UserRole.UNKNOWN
)

@HiltViewModel
class LaboratorProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LaboratorProfileUiState())
    val state: StateFlow<LaboratorProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is ApiResult.Success -> {
                    val user = result.data
                    _state.update {
                        it.copy(
                            displayName = listOfNotNull(user.firstName, user.lastName)
                                .filter { part -> part.isNotBlank() }
                                .joinToString(" ")
                                .ifBlank { user.email },
                            email = user.email,
                            role = user.role
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}
