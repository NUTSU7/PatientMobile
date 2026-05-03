package com.semanticsoft.patientmobile.ui.screens.registration

import androidx.lifecycle.SavedStateHandle
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.FieldError
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.exceptions.ApiValidationException
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
        val vm = RegistrationViewModel(FakeAuthRepository(), SavedStateHandle())

        vm.register()
        advanceUntilIdle()

        assertTrue(vm.state.fieldErrors.isNotEmpty())
        assertFalse(vm.state.isLoading)
    }

    @Test
    fun register_surfacesApiFieldErrors() = runTest {
        val apiEx = ApiValidationException(
            message = "Validation failed",
            fieldErrors = listOf(FieldError("email", "Already used", "john@example.com"))
        )
        val vm = RegistrationViewModel(FakeAuthRepository(throwOnRegister = apiEx), SavedStateHandle())
        vm.onNameChange("John Doe")
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("Sup3rStrongPassword!")
        vm.onConfirmPasswordChange("Sup3rStrongPassword!")

        vm.register()
        advanceUntilIdle()

        assertEquals("Already used", vm.state.fieldErrors["email"])
        assertEquals("Validation failed", vm.state.errorMessage)
    }

    @Test
    fun register_success_clearsErrors() = runTest {
        val vm = RegistrationViewModel(FakeAuthRepository(), SavedStateHandle())
        vm.onNameChange("John Doe")
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("Sup3rStrongPassword!")
        vm.onConfirmPasswordChange("Sup3rStrongPassword!")

        vm.register()
        advanceUntilIdle()

        assertTrue(vm.state.errorMessage == null)
        assertTrue(vm.state.fieldErrors.isEmpty())
    }

    private class FakeAuthRepository(
        private val throwOnRegister: Throwable? = null
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): AuthResponse = response(email)

        override suspend fun register(request: RegisterRequest): AuthResponse {
            throwOnRegister?.let { throw it }
            return response(request.email)
        }

        override suspend fun refresh(): AuthResponse = response("john@example.com")
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): User = User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"))

        private fun response(email: String): AuthResponse {
            return AuthResponse(
                accessToken = "a",
                refreshToken = "r",
                tokenType = "BEARER",
                expiresIn = 1,
                refreshExpiresIn = 1,
                user = User("u1", email, "John", "Doe", LocalDate.parse("1990-01-01"))
            )
        }
    }
}
