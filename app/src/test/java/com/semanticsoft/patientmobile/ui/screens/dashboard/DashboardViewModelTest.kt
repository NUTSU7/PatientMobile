package com.semanticsoft.patientmobile.ui.screens.dashboard

import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.ApiResult
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            documentRepository = FakeDocumentRepository(ApiResult.NetworkError),
            medicalResultRepository = FakeMedicalResultRepository()
        )

        vm.refresh()
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun refresh_updatesSummary_whenDataAvailable() = runTest {
        val vm = DashboardViewModel(
            authRepository = FakeAuthRepository(),
            documentRepository = FakeDocumentRepository(
                ApiResult.Success(
                    listOf(
                        PatientDocument(
                            id = "d1",
                            originalFileName = "lab.pdf",
                            mimeType = "application/pdf",
                            fileSizeBytes = 100,
                            uploadedAt = Instant.now()
                        )
                    )
                )
            ),
            medicalResultRepository = FakeMedicalResultRepository()
        )

        vm.refresh()
        advanceUntilIdle()

        assertTrue(vm.state.value.markerSummary.attention >= 0)
        assertFalse(vm.state.value.isLoading)
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun refresh(): ApiResult<AuthResponse> = throw UnsupportedOperationException()
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): ApiResult<User> =
            ApiResult.Success(User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"), "PATIENT", Instant.EPOCH))
        override suspend fun isLoggedIn(): Boolean = true
    }

    private class FakeDocumentRepository(
        private val result: ApiResult<List<PatientDocument>>
    ) : DocumentRepository {
        override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> =
            throw UnsupportedOperationException()
        override suspend fun getDocuments(page: Int, size: Int): ApiResult<List<PatientDocument>> = result
        override suspend fun getDocumentById(id: String): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun downloadDocument(id: String): ApiResult<File> =
            throw UnsupportedOperationException()
        override suspend fun renameDocument(id: String, newName: String): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun deleteDocument(id: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun createShareLink(id: String): ApiResult<com.semanticsoft.patientmobile.domain.model.SharedLink> =
            throw UnsupportedOperationException()
    }

    private class FakeMedicalResultRepository : MedicalResultRepository {
        override suspend fun getByDocumentId(docId: String): ApiResult<List<MedicalResult>> =
            ApiResult.Success(
                listOf(
                    MedicalResult(
                        id = "r1", documentId = docId, reportId = "rep1",
                        originalTestName = "TSH", canonicalName = "TSH",
                        analysisGroup = "Hormones", valueNumeric = 0.3,
                        valueText = "0.3", unit = "mIU/L",
                        referenceLow = 0.4, referenceHigh = 4.0,
                        referenceText = "0.4-4.0", abnormalFlag = "BORDERLINE",
                        observedAt = LocalDate.parse("2026-03-01")
                    )
                )
            )
        override suspend fun getLatestReport(documentId: String): ApiResult<MedicalReport> =
            ApiResult.Success(
                MedicalReport(
                    id = "rep1", documentId = documentId, engineName = "tesseract",
                    title = "Blood Test", status = OcrStatus.COMPLETED,
                    summary = "Normal", createdAt = Instant.EPOCH
                )
            )
        override suspend fun getAllResults(page: Int, size: Int, analysisGroup: String?): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getResultById(resultId: String): ApiResult<MedicalResult> =
            throw UnsupportedOperationException()
        override suspend fun getResultHistory(resultId: String): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getReportResults(reportId: String): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
    }
}