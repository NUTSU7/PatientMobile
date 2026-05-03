package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.local.dao.UserDao
import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.dto.AuthResponse as AuthResponseDto
import com.semanticsoft.patientmobile.data.remote.api.dto.LoginRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.UserDto
import com.semanticsoft.patientmobile.domain.model.FieldError
import com.semanticsoft.patientmobile.util.exceptions.ApiValidationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = com.semanticsoft.patientmobile.testutil.MainDispatcherRule()

    private val apiService: PatientApiService = mock()
    private val userDao: UserDao = mock()
    private val tokenManager: TokenManager = mock()

    @Test
    fun login_savesTokensAndCachesUser() = runTest {
        val dto = AuthResponseDto(
            accessToken = "access",
            refreshToken = "refresh",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshExpiresIn = 7200,
            user = UserDto("u1", "john@example.com", "John", "Doe", "1990-01-01")
        )
        whenever(apiService.login(LoginRequest("john@example.com", "password"))).thenReturn(Response.success(dto))

        val repository = AuthRepositoryImpl(apiService, userDao, tokenManager)
        val result = repository.login("john@example.com", "password")

        assertEquals("access", result.accessToken)
        verify(tokenManager).saveTokens("access", "refresh")
        verify(userDao).insert(any())
    }

    @Test
    fun register_throwsValidationException_whenApiReturnsFieldErrors() = runTest {
        val request = RegisterRequest(
            email = "john@example.com",
            password = "Sup3rStrongPassword!",
            confirmPassword = "Sup3rStrongPassword!",
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = "1990-01-01"
        )

        val errorJson = """
            {
              "message": "Validation failed",
              "fieldErrors": [{"field":"email","message":"Already used","rejectedValue":"john@example.com"}]
            }
        """.trimIndent()

        whenever(apiService.register(request)).thenReturn(
            Response.error(400, errorJson.toResponseBody("application/json".toMediaType()))
        )

        val repository = AuthRepositoryImpl(apiService, userDao, tokenManager)

        val throwable = kotlin.runCatching { repository.register(request) }.exceptionOrNull()

        assert(throwable is ApiValidationException)
        val fieldErrors = (throwable as ApiValidationException).fieldErrors
        assertEquals(FieldError("email", "Already used", "john@example.com"), fieldErrors.first())
    }

    @Test
    fun refresh_usesStoredRefreshToken() = runTest {
        whenever(tokenManager.getRefreshToken()).thenReturn("r1")
        val dto = AuthResponseDto(
            accessToken = "a2",
            refreshToken = "r2",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshExpiresIn = 7200,
            user = UserDto("u1", "john@example.com", "John", "Doe", "1990-01-01")
        )
        whenever(apiService.refresh(RefreshRequest("r1"))).thenReturn(Response.success(dto))

        val repository = AuthRepositoryImpl(apiService, userDao, tokenManager)
        repository.refresh()

        verify(tokenManager).saveTokens("a2", "r2")
    }
}
