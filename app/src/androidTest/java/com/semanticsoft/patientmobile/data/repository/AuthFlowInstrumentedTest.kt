package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.data.remote.api.ResponseEntity
import com.semanticsoft.patientmobile.data.remote.api.dto.AuthResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.LoginRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.PaginatedResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.UserDto
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.User
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthFlowInstrumentedTest {

    private val tokenManager = InMemoryTokenManager()
    private val repository = AuthRepositoryImpl(FakePatientApiService(), tokenManager)

    @Test
    fun registerLoginRefreshLogout_flow() = runBlocking {
        val registerResult = repository.register(
            RegisterRequest(
                email = "john@example.com",
                password = "Sup3rStrongPassword!",
                confirmPassword = "Sup3rStrongPassword!",
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1990-01-01"
            )
        )
        assertNotNull(registerResult)
        assertEquals("john@example.com", (registerResult as? com.semanticsoft.patientmobile.util.ApiResult.Success)?.data?.user?.email)
        assertNotNull(tokenManager.getAccessToken())

        val loginResult = repository.login("john@example.com", "Sup3rStrongPassword!")
        assertEquals("john@example.com", (loginResult as? com.semanticsoft.patientmobile.util.ApiResult.Success)?.data?.user?.email)

        val refreshResult = repository.refresh()
        assertEquals("new_access", (refreshResult as? com.semanticsoft.patientmobile.util.ApiResult.Success)?.data?.accessToken)

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

    private class FakePatientApiService : PatientApiService {
        override suspend fun register(request: RegisterRequest): ResponseEntity<AuthResponse> {
            return retrofit2.Response.success(auth("access", "refresh", request.email))
        }

        override suspend fun login(request: LoginRequest): AuthResponse {
            return auth("access", "refresh", request.email)
        }

        override suspend fun refresh(request: RefreshRequest): AuthResponse {
            return auth("new_access", "new_refresh", "john@example.com")
        }

        override suspend fun logout(refreshToken: String) = Unit

        override suspend fun getCurrentUser(): User {
            return User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"), "PATIENT", Instant.EPOCH)
        }

        override suspend fun uploadDocument(file: MultipartBody.Part): PatientDocument {
            return PatientDocument(
                id = "d1",
                originalFileName = "doc.pdf",
                mimeType = "application/pdf",
                fileSizeBytes = 100,
                uploadedAt = Instant.now()
            )
        }

        override suspend fun getDocuments(page: Int, size: Int): PaginatedResponse<PatientDocument> {
            return PaginatedResponse(emptyList(), page, size, 0, 0)
        }

        override suspend fun getDocumentById(id: String): PatientDocument {
            return PatientDocument(
                id = id,
                originalFileName = "doc.pdf",
                mimeType = "application/pdf",
                fileSizeBytes = 100,
                uploadedAt = Instant.now()
            )
        }

        override suspend fun downloadDocument(id: String): ResponseBody {
            throw UnsupportedOperationException()
        }

        override suspend fun getMedicalResultsByDocument(documentId: String): List<MedicalResult> {
            return emptyList()
        }

        override suspend fun getMedicalResults(documentId: String): List<MedicalResult> {
            return emptyList()
        }

        private fun auth(access: String, refresh: String, email: String): AuthResponse {
            return AuthResponse(
                accessToken = access,
                refreshToken = refresh,
                tokenType = "Bearer",
                expiresIn = 3600,
                refreshExpiresIn = 7200,
                user = UserDto(
                    id = "u1",
                    email = email,
                    firstName = "John",
                    lastName = "Doe",
                    dateOfBirth = "1990-01-01"
                )
            )
        }
    }
}