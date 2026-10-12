package com.semanticsoft.patientmobile.data.repository.common

import com.semanticsoft.patientmobile.core.datastore.TokenManager
import com.semanticsoft.patientmobile.core.network.api.AuthApiService
import com.semanticsoft.patientmobile.core.network.dto.auth.AuthResponse
import com.semanticsoft.patientmobile.core.network.dto.auth.ChangePasswordRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.DeleteAccountRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.LoginRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.LogoutRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.RefreshRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.RefreshResponse
import com.semanticsoft.patientmobile.core.network.dto.auth.RegisterRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.UserDto
import com.semanticsoft.patientmobile.domain.model.common.RegistrationRequest
import com.semanticsoft.patientmobile.domain.model.common.User
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthFlowInstrumentedTest {

    private val tokenManager = InMemoryTokenManager()
    private val repository = AuthRepositoryImpl(FakeAuthApiService(), tokenManager)

    @Test
    fun registerLoginRefreshLogout_flow() = runBlocking {
        val registerResult = repository.register(
            RegistrationRequest(
                email = "john@example.com",
                password = "Sup3rStrongPassword!",
                confirmPassword = "Sup3rStrongPassword!",
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1990-01-01"
            )
        )
        assertNotNull(registerResult)
        assertEquals("john@example.com", (registerResult as? com.semanticsoft.patientmobile.core.util.ApiResult.Success)?.data?.user?.email)
        assertNotNull(tokenManager.getAccessToken())

        val loginResult = repository.login("john@example.com", "Sup3rStrongPassword!")
        assertEquals("john@example.com", (loginResult as? com.semanticsoft.patientmobile.core.util.ApiResult.Success)?.data?.user?.email)

        val refreshResult = repository.refresh()
        assertEquals("new_access", (refreshResult as? com.semanticsoft.patientmobile.core.util.ApiResult.Success)?.data?.accessToken)

        assertTrue(tokenManager.getUser() != null)

        repository.logout()
        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getUser())
    }

    @Test
    fun isLoggedIn_returnsFalse_afterLogout() = runBlocking {
        tokenManager.saveTokens("access", "refresh")
        assertTrue(repository.isLoggedIn())

        tokenManager.clearTokens()
        assertFalse(repository.isLoggedIn())
    }

    private fun assertTrue(value: Boolean) {
        org.junit.Assert.assertTrue(value)
    }

    private fun assertFalse(value: Boolean) {
        org.junit.Assert.assertFalse(value)
    }

    private class InMemoryTokenManager : TokenManager {
        private var access: String? = null
        private var refresh: String? = null
        private var user: User? = null

        override fun saveTokens(access: String, refresh: String) {
            this.access = access
            this.refresh = refresh
        }

        override fun getAccessToken(): String? = access
        override fun getRefreshToken(): String? = refresh

        override fun clearTokens() {
            access = null
            refresh = null
        }

        override fun saveUser(user: User) {
            this.user = user
        }

        override fun getUser(): User? = user

        override fun clearUser() {
            user = null
        }
    }

    private class FakeAuthApiService : AuthApiService {
        override suspend fun register(request: RegisterRequest): AuthResponse =
            auth("access", "refresh", request.email)

        override suspend fun login(request: LoginRequest): AuthResponse =
            auth("access", "refresh", request.email)

        override suspend fun refresh(request: RefreshRequest): RefreshResponse =
            RefreshResponse(
                accessToken = "new_access",
                refreshToken = "new_refresh",
                tokenType = "Bearer",
                expiresIn = 3600,
                refreshExpiresIn = 7200,
                user = userDto("john@example.com")
            )

        override suspend fun logout(request: LogoutRequest) = Unit

        override suspend fun getCurrentUser(): UserDto = userDto("john@example.com")

        override suspend fun changePassword(request: ChangePasswordRequest) = Unit

        override suspend fun deleteAccount(request: DeleteAccountRequest) = Unit

        private fun auth(access: String, refresh: String, email: String): AuthResponse {
            return AuthResponse(
                accessToken = access,
                refreshToken = refresh,
                tokenType = "Bearer",
                expiresIn = 3600,
                refreshExpiresIn = 7200,
                user = userDto(email)
            )
        }

        private fun userDto(email: String): UserDto = UserDto(
            id = "u1",
            email = email,
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = "1990-01-01"
        )
    }
}
