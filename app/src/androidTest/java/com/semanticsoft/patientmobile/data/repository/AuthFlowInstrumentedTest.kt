package com.semanticsoft.patientmobile.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.semanticsoft.patientmobile.data.local.dao.UserDao
import com.semanticsoft.patientmobile.data.local.datastore.TokenManager
import com.semanticsoft.patientmobile.data.local.db.PatientDatabase
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
import com.semanticsoft.patientmobile.domain.model.SyncStatus
import com.semanticsoft.patientmobile.domain.model.User
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class AuthFlowInstrumentedTest {

    private lateinit var db: PatientDatabase
    private lateinit var userDao: UserDao
    private lateinit var tokenManager: InMemoryTokenManager
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PatientDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = db.userDao()
        tokenManager = InMemoryTokenManager()
        repository = AuthRepositoryImpl(FakePatientApiService(), userDao, tokenManager)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun registerLoginRefreshLogout_flow() = runBlocking {
        val registerResponse = repository.register(
            RegisterRequest(
                email = "john@example.com",
                password = "Sup3rStrongPassword!",
                confirmPassword = "Sup3rStrongPassword!",
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1990-01-01"
            )
        )
        assertEquals("john@example.com", registerResponse.user.email)
        assertNotNull(tokenManager.getAccessToken())

        val loginResponse = repository.login("john@example.com", "Sup3rStrongPassword!")
        assertEquals("john@example.com", loginResponse.user.email)

        val refreshResponse = repository.refresh()
        assertEquals("new_access", refreshResponse.accessToken)

        repository.logout()
        assertEquals(null, tokenManager.getAccessToken())
        assertEquals(null, userDao.getUser())
    }

    private class InMemoryTokenManager : TokenManager {
        private var access: String? = null
        private var refresh: String? = null

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
    }

    private class FakePatientApiService : PatientApiService {
        override suspend fun register(request: RegisterRequest): ResponseEntity<AuthResponse> {
            return Response.success(auth("access", "refresh", request.email))
        }

        override suspend fun login(request: LoginRequest): AuthResponse {
            return auth("access", "refresh", request.email)
        }

        override suspend fun refresh(request: RefreshRequest): AuthResponse {
            return auth("new_access", "new_refresh", "john@example.com")
        }

        override suspend fun logout(refreshToken: String) = Unit

        override suspend fun getCurrentUser(): User {
            return User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"))
        }

        override suspend fun uploadDocument(file: MultipartBody.Part): PatientDocument {
            return PatientDocument(
                id = "d1",
                ownerUserId = "u1",
                originalFileName = "doc.pdf",
                mimeType = "application/pdf",
                fileSizeBytes = 100,
                uploadedAt = Instant.now(),
                syncStatus = SyncStatus.SYNCED
            )
        }

        override suspend fun getDocuments(page: Int, size: Int): PaginatedResponse<PatientDocument> {
            return PaginatedResponse(emptyList(), page, size, 0, 0)
        }

        override suspend fun getDocumentById(id: String): PatientDocument {
            return PatientDocument(
                id = id,
                ownerUserId = "u1",
                originalFileName = "doc.pdf",
                mimeType = "application/pdf",
                fileSizeBytes = 100,
                uploadedAt = Instant.now(),
                syncStatus = SyncStatus.SYNCED
            )
        }

        override suspend fun downloadDocument(id: String): ResponseBody {
            throw UnsupportedOperationException()
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
