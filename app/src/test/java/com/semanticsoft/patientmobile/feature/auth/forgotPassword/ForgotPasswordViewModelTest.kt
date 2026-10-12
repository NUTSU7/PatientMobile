package com.semanticsoft.patientmobile.feature.auth.forgotPassword

import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.model.common.AuthResponse
import com.semanticsoft.patientmobile.domain.model.common.RegistrationRequest
import com.semanticsoft.patientmobile.domain.model.common.User
import com.semanticsoft.patientmobile.domain.model.common.UserRole
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.domain.usecase.common.RequestPasswordResetUseCase
import com.semanticsoft.patientmobile.domain.usecase.common.ResetPasswordUseCase
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createVm(
        forgotResult: ApiResult<Unit> = ApiResult.Success(Unit),
        resetResult: ApiResult<Unit> = ApiResult.Success(Unit)
    ): ForgotPasswordViewModel {
        val repo = FakeAuthRepository(forgotResult = forgotResult, resetResult = resetResult)
        val vm = ForgotPasswordViewModel(
            RequestPasswordResetUseCase(repo),
            ResetPasswordUseCase(repo)
        )
        forgotCallCount = { repo.forgotCallCount }
        return vm
    }

    private lateinit var forgotCallCount: () -> Int

    @Test
    fun requestSubmit_rejectsInvalidEmail() = runTest {
        val vm = createVm()

        vm.onEmailChange("not-an-email")
        vm.onRequestSubmit()
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.REQUEST, vm.state.value.step)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun requestSubmit_success_movesToConfirmAndStartsCooldown() = runTest {
        val vm = createVm()

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        runCurrent()

        assertEquals(ForgotPasswordStep.CONFIRM, vm.state.value.step)
        assertFalse(vm.state.value.isLoading)
        assertEquals(120, vm.state.value.resendCooldownSeconds)
    }

    @Test
    fun requestSubmit_http429_setsErrorAndStartsCooldown() = runTest {
        val vm = createVm(forgotResult = ApiResult.HttpError(429, "Too Many Requests"))

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        runCurrent()

        assertEquals(ForgotPasswordStep.REQUEST, vm.state.value.step)
        assertEquals("Prea multe încercări. Încearcă din nou în câteva minute.", vm.state.value.errorMessage)
        assertEquals(120, vm.state.value.resendCooldownSeconds)
    }

    @Test
    fun requestSubmit_networkError_setsMessage() = runTest {
        val vm = createVm(forgotResult = ApiResult.NetworkError)

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        runCurrent()

        assertEquals("Nu există conexiune la internet.", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun cooldown_countsDownOverTime() = runTest {
        val vm = createVm()

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        runCurrent()
        assertEquals(120, vm.state.value.resendCooldownSeconds)

        advanceTimeBy(30_000)
        runCurrent()

        assertEquals(90, vm.state.value.resendCooldownSeconds)
    }

    @Test
    fun codeEntry_autoAdvancesToSetNew_afterSixDigits() = runTest {
        val vm = createVm()
        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        advanceUntilIdle()

        vm.onCodeChange("123456")

        assertEquals(ForgotPasswordStep.SET_NEW, vm.state.value.step)
        assertEquals("123456", vm.state.value.code)
    }

    @Test
    fun resend_ignoredDuringCooldown() = runTest {
        val vm = createVm()
        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        runCurrent()

        vm.onResendClick()
        runCurrent()

        assertEquals(ForgotPasswordStep.CONFIRM, vm.state.value.step)
        assertEquals(1, forgotCallCount())
    }

    @Test
    fun submitNewPassword_invalidCriteria_doesNotCallApi() = runTest {
        val vm = createVm(resetResult = ApiResult.HttpError(-1, "should not be called"))

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        advanceUntilIdle()
        vm.onCodeChange("123456")
        vm.onNewPasswordChange("weak")
        vm.onConfirmPasswordChange("weak")
        vm.onSubmitNewPassword()
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.SET_NEW, vm.state.value.step)
        assertNull(vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun submitNewPassword_success_emitsPasswordChanged() = runTest {
        val vm = createVm()
        val events = mutableListOf<ForgotPasswordEvent>()
        val collectJob = launch { vm.events.collect { events.add(it) } }

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        advanceUntilIdle()
        vm.onCodeChange("123456")
        vm.onNewPasswordChange("Sup3rStrongPass!")
        vm.onConfirmPasswordChange("Sup3rStrongPass!")
        vm.onSubmitNewPassword()
        advanceUntilIdle()
        collectJob.cancel()
        advanceUntilIdle()

        assertTrue(events.any { it is ForgotPasswordEvent.PasswordChanged })
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun submitNewPassword_authError_movesToErrorCard() = runTest {
        val vm = createVm(resetResult = ApiResult.AuthError)

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        advanceUntilIdle()
        vm.onCodeChange("123456")
        vm.onNewPasswordChange("Sup3rStrongPass!")
        vm.onConfirmPasswordChange("Sup3rStrongPass!")
        vm.onSubmitNewPassword()
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.ERROR, vm.state.value.step)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun submitNewPassword_http400_showsInlineError_staysOnCard() = runTest {
        val vm = createVm(resetResult = ApiResult.HttpError(400, "Parola a fost folosită recent."))

        vm.onEmailChange("john@example.com")
        vm.onRequestSubmit()
        advanceUntilIdle()
        vm.onCodeChange("123456")
        vm.onNewPasswordChange("Sup3rStrongPass!")
        vm.onConfirmPasswordChange("Sup3rStrongPass!")
        vm.onSubmitNewPassword()
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.SET_NEW, vm.state.value.step)
        assertEquals("Parola a fost folosită recent.", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    private class FakeAuthRepository(
        private val forgotResult: ApiResult<Unit>,
        private val resetResult: ApiResult<Unit>
    ) : AuthRepository {
        var forgotCallCount = 0
            private set
        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun register(request: RegistrationRequest): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun refresh(): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): ApiResult<User> =
            ApiResult.Success(User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"), UserRole.PATIENT, Instant.EPOCH))
        override suspend fun isLoggedIn(): Boolean = true
        override suspend fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun deleteAccount(password: String, confirmation: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun requestPasswordReset(email: String): ApiResult<Unit> {
            forgotCallCount++
            return forgotResult
        }
        override suspend fun resetPassword(
            email: String,
            code: String,
            newPassword: String,
            confirmNewPassword: String
        ): ApiResult<Unit> = resetResult
    }
}