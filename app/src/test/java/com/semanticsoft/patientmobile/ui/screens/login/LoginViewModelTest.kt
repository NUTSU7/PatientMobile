package com.semanticsoft.patientmobile.ui.screens.login

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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_setsError_forEmptyCredentials() = runTest {
        val vm = LoginViewModel(FakeAuthRepository())

        vm.login()
        advanceUntilIdle()

        assertNull(vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun login_emitsError_forInvalidCredentials() = runTest {
        val vm = LoginViewModel(FakeAuthRepository(loginResult = ApiResult.AuthError))
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("wrong")

        vm.login()
        advanceUntilIdle()

        assertEquals("Email sau parolă incorecte.", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun login_emitsError_forHttp401() = runTest {
        val vm = LoginViewModel(
            FakeAuthRepository(loginResult = ApiResult.HttpError(401, "Unauthorized"))
        )
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("pass")

        vm.login()
        advanceUntilIdle()

        assertEquals("Email sau parolă incorecte.", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun login_emitsError_forNetworkError() = runTest {
        val vm = LoginViewModel(
            FakeAuthRepository(loginResult = ApiResult.NetworkError)
        )
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("pass")

        vm.login()
        advanceUntilIdle()

        assertEquals("Nu există conexiune la internet.", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun login_success_clearsError() = runTest {
        val vm = LoginViewModel(FakeAuthRepository())
        vm.onEmailChange("john@example.com")
        vm.onPasswordChange("Sup3rStrongPassword!")

        vm.login()
        advanceUntilIdle()

        assertNull(vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    private class FakeAuthRepository(
        private val loginResult: ApiResult<AuthResponse> = ApiResult.Success(
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
        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> = loginResult

        override suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> =
            login(request.email, request.password)

        override suspend fun refresh(): ApiResult<AuthResponse> = loginResult
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