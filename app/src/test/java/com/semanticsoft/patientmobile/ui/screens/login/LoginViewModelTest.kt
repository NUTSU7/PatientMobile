package com.semanticsoft.patientmobile.ui.screens.login

import androidx.lifecycle.SavedStateHandle
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.exceptions.InvalidCredentialsException
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_setsError_forEmptyCredentials() = runTest {
        val vm = LoginViewModel(FakeAuthRepository(), SavedStateHandle())

        vm.login()
        advanceUntilIdle()

        assertEquals("Completează emailul și parola.", vm.state.errorMessage)
        assertFalse(vm.state.isLoading)
    }

    @Test
    fun login_emitsError_forInvalidCredentials() = runTest {
        val vm = LoginViewModel(FakeAuthRepository(throwOnLogin = InvalidCredentialsException("bad")), SavedStateHandle())
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("wrong")

        vm.login()
        advanceUntilIdle()

        assertEquals("Email sau parolă incorecte.", vm.state.errorMessage)
        assertFalse(vm.state.isLoading)
    }

    @Test
    fun login_success_clearsError() = runTest {
        val vm = LoginViewModel(FakeAuthRepository(), SavedStateHandle())
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("Sup3rStrongPassword!")

        vm.login()
        advanceUntilIdle()

        assertTrue(vm.state.errorMessage == null)
        assertFalse(vm.state.isLoading)
    }

    private class FakeAuthRepository(
        private val throwOnLogin: Throwable? = null
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): AuthResponse {
            throwOnLogin?.let { throw it }
            return AuthResponse(
                accessToken = "a",
                refreshToken = "r",
                tokenType = "BEARER",
                expiresIn = 1,
                refreshExpiresIn = 1,
                user = User("u1", email, "John", "Doe", LocalDate.parse("1990-01-01"))
            )
        }

        override suspend fun register(request: RegisterRequest): AuthResponse = login(request.email, request.password)
        override suspend fun refresh(): AuthResponse = login("john@example.com", "x")
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): User = User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"))
    }
}
