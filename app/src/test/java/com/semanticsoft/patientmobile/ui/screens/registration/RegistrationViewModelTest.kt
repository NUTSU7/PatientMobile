package com.semanticsoft.patientmobile.ui.screens.registration

import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.ApiResult
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun register_setsFieldErrors_forInvalidLocalInput() = runTest {
        val vm = RegistrationViewModel(FakeAuthRepository())

        vm.register()
        advanceUntilIdle()

        assertTrue(vm.state.value.fieldErrors.isNotEmpty())
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun register_surfacesApiHttpError() = runTest {
        val vm = RegistrationViewModel(
            FakeAuthRepository(registerResult = ApiResult.HttpError(422, "Already used"))
        )
        vm.onNameChange("John Doe")
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("Sup3rStrongPassword!")
        vm.onConfirmPasswordChange("Sup3rStrongPassword!")

        vm.register()
        advanceUntilIdle()

        assertEquals("Date invalide. Verifică câmpurile.", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun register_emitsError_forNetworkError() = runTest {
        val vm = RegistrationViewModel(
            FakeAuthRepository(registerResult = ApiResult.NetworkError)
        )
        vm.onNameChange("John Doe")
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("Sup3rStrongPassword!")
        vm.onConfirmPasswordChange("Sup3rStrongPassword!")

        vm.register()
        advanceUntilIdle()

        assertEquals("Nu există conexiune la internet.", vm.state.value.errorMessage)
    }

    @Test
    fun register_success_clearsErrors() = runTest {
        val vm = RegistrationViewModel(FakeAuthRepository())
        vm.onNameChange("John Doe")
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("Sup3rStrongPassword!")
        vm.onConfirmPasswordChange("Sup3rStrongPassword!")

        vm.register()
        advanceUntilIdle()

        assertTrue(vm.state.value.errorMessage == null)
        assertTrue(vm.state.value.fieldErrors.isEmpty())
    }

    private class FakeAuthRepository(
        private val registerResult: ApiResult<AuthResponse> = ApiResult.Success(
            AuthResponse(
                accessToken = "a",
                refreshToken = "r",
                tokenType = "BEARER",
                expiresIn = 1,
                refreshExpiresIn = 1,
                user = User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"), "PATIENT", Instant.EPOCH)
            )
        )
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> = registerResult
        override suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> = registerResult
        override suspend fun refresh(): ApiResult<AuthResponse> = registerResult
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): ApiResult<User> =
            ApiResult.Success(User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"), "PATIENT", Instant.EPOCH))
        override suspend fun isLoggedIn(): Boolean = true
        override suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun deleteAccount(): ApiResult<Unit> =
            throw UnsupportedOperationException()
    }
}