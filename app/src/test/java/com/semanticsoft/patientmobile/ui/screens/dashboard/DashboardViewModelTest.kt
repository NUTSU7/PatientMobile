package com.semanticsoft.patientmobile.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.SyncStatus
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.Resource
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refresh_setsError_whenDocumentsFail() = runTest {
        val vm = DashboardViewModel(
            authRepository = FakeAuthRepository(),
            documentRepository = FakeDocumentRepository(Resource.Error("boom")),
            medicalResultRepository = FakeMedicalResultRepository(),
            savedStateHandle = SavedStateHandle()
        )

        vm.refresh()
        advanceUntilIdle()

        assertTrue(vm.state.errorMessage == "boom")
        assertFalse(vm.state.isLoading)
    }

    @Test
    fun refresh_updatesSummary_whenDataAvailable() = runTest {
        val vm = DashboardViewModel(
            authRepository = FakeAuthRepository(),
            documentRepository = FakeDocumentRepository(
                Resource.Success(
                    listOf(
                        PatientDocument(
                            id = "d1",
                            ownerUserId = "u1",
                            originalFileName = "lab.pdf",
                            mimeType = "application/pdf",
                            fileSizeBytes = 100,
                            uploadedAt = Instant.now(),
                            syncStatus = SyncStatus.SYNCED
                        )
                    )
                )
            ),
            medicalResultRepository = FakeMedicalResultRepository(),
            savedStateHandle = SavedStateHandle()
        )

        vm.refresh()
        advanceUntilIdle()

        assertTrue(vm.state.markerSummary.attention >= 0)
        assertFalse(vm.state.isLoading)
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): AuthResponse = throw UnsupportedOperationException()
        override suspend fun register(request: RegisterRequest): AuthResponse = throw UnsupportedOperationException()
        override suspend fun refresh(): AuthResponse = throw UnsupportedOperationException()
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): User = User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"))
    }

    private class FakeDocumentRepository(
        private val result: Resource<List<PatientDocument>>
    ) : DocumentRepository {
        override suspend fun uploadDocument(file: File): PatientDocument = throw UnsupportedOperationException()
        override suspend fun checkDuplicates(checksums: List<String>): List<DocumentDuplicateInfo> = emptyList()
        override fun getDocuments(): Flow<Resource<List<PatientDocument>>> = flowOf(Resource.Loading, result)
        override fun getDocumentById(id: String): Flow<Resource<PatientDocument>> = throw UnsupportedOperationException()
        override suspend fun downloadDocument(id: String): File = throw UnsupportedOperationException()
    }

    private class FakeMedicalResultRepository : MedicalResultRepository {
        override fun getByDocumentId(docId: String): Flow<Resource<List<MedicalResult>>> {
            return flowOf(
                Resource.Loading,
                Resource.Success(
                    listOf(
                        MedicalResult(
                            id = "r1",
                            documentId = docId,
                            reportId = "rep1",
                            analysisType = "blood",
                            testName = "TSH",
                            value = "0.3",
                            unit = "mIU/L",
                            referenceRange = "0.4-4.0",
                            reportDate = LocalDate.parse("2026-03-01")
                        )
                    )
                )
            )
        }

        override suspend fun sync(docId: String): Resource<List<MedicalResult>> = Resource.Success(emptyList())
    }
}
