package com.semanticsoft.patientmobile.ui.screens.dashboard

import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.domain.model.PaginatedMedicalResults
import com.semanticsoft.patientmobile.domain.model.AuthResponse
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.ReportWithResults
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.data.remote.api.dto.AiSummaryResponse
import com.semanticsoft.patientmobile.domain.repository.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.ApiResult
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
            dashboardRepository = FakeDashboardRepository(),
            medicalResultRepository = FakeMedicalResultRepository(),
            documentRepository = FakeDocumentRepository(ApiResult.NetworkError),
            globalSyncManager = FakeGlobalSyncManager()
        )

        vm.refresh()
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun refresh_updatesSummary_whenDataAvailable() = runTest {
        val vm = DashboardViewModel(
            authRepository = FakeAuthRepository(),
            dashboardRepository = FakeDashboardRepository(),
            medicalResultRepository = FakeMedicalResultRepository(),
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
            globalSyncManager = FakeGlobalSyncManager()
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
        override suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun deleteAccount(): ApiResult<Unit> =
            throw UnsupportedOperationException()
    }

    private class FakeDashboardRepository : DashboardRepository {
        override suspend fun getAiSummary(): ApiResult<AiSummaryResponse> =
            ApiResult.Success(AiSummaryResponse(summaryText = "Summary", status = "READY"))
        override suspend fun regenerateAiSummary(): ApiResult<AiSummaryResponse> =
            ApiResult.Success(AiSummaryResponse(summaryText = "Regenerated", status = "READY"))
        override suspend fun requestExplanation(reportId: String): ApiResult<String> =
            throw UnsupportedOperationException()
    }

    private class FakeDocumentRepository(
        private val result: ApiResult<List<PatientDocument>>
    ) : DocumentRepository {
        override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> =
            throw UnsupportedOperationException()
        override suspend fun getDocuments(
            page: Int, size: Int, search: String?, dateFrom: String?, dateTo: String?,
            sortBy: String?, sortDir: String?
        ): ApiResult<List<PatientDocument>> = result
        override suspend fun getDocumentById(id: String): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun downloadDocument(id: String): ApiResult<File> =
            throw UnsupportedOperationException()
        override suspend fun downloadDocumentFile(documentId: String): ApiResult<File> =
            throw UnsupportedOperationException()
        override suspend fun getDocumentExplanation(documentId: String): ApiResult<String> =
            throw UnsupportedOperationException()
        override suspend fun renameDocument(id: String, newName: String): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun deleteDocument(id: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun bulkDelete(documentIds: List<String>): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun createShareLink(id: String): ApiResult<com.semanticsoft.patientmobile.domain.model.SharedLink> =
            throw UnsupportedOperationException()
        override suspend fun getDocumentStats(): ApiResult<com.semanticsoft.patientmobile.domain.model.DocumentStats> =
            throw UnsupportedOperationException()
    }

    private class FakeMedicalResultRepository(
        private val latestResults: List<MedicalResult> = listOf(
            MedicalResult(
                id = "r1", documentId = "d1", reportId = "rep1",
                originalTestName = "TSH", canonicalName = "TSH",
                analysisGroup = "Hormones", valueNumeric = 0.3,
                valueText = "0.3", unit = "mIU/L",
                referenceLow = 0.4, referenceHigh = 4.0,
                referenceText = "0.4-4.0", abnormalFlag = "LOW",
                observedAt = LocalDate.parse("2026-03-01")
            ),
            MedicalResult(
                id = "r2", documentId = "d1", reportId = "rep1",
                originalTestName = "Glucose", canonicalName = "Glucose",
                analysisGroup = "Biochemistry", valueNumeric = 95.0,
                valueText = "95", unit = "mg/dL",
                referenceLow = 70.0, referenceHigh = 100.0,
                referenceText = "70-100", abnormalFlag = "NORMAL",
                observedAt = LocalDate.parse("2026-03-01")
            ),
            MedicalResult(
                id = "r3", documentId = "d1", reportId = "rep1",
                originalTestName = "Creatinine", canonicalName = "Creatinine",
                analysisGroup = "Biochemistry", valueNumeric = 1.5,
                valueText = "1.5", unit = "mg/dL",
                referenceLow = 0.6, referenceHigh = 1.3,
                referenceText = "0.6-1.3", abnormalFlag = "HIGH",
                observedAt = LocalDate.parse("2026-03-01")
            )
        )
    ) : MedicalResultRepository {
        override suspend fun getByDocumentId(docId: String): ApiResult<List<MedicalResult>> =
            ApiResult.Success(latestResults)
        override suspend fun getLatestReport(documentId: String): ApiResult<MedicalReport> =
            ApiResult.Success(
                MedicalReport(
                    id = "rep1", documentId = documentId, engineName = "tesseract",
                    title = "Blood Test", status = OcrStatus.COMPLETED,
                    summary = "Normal", createdAt = Instant.EPOCH
                )
            )
        override suspend fun getAllResults(page: Int, size: Int, analysisGroup: String?, sortBy: String?, sortDir: String?): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getAllResultsPaginated(page: Int, size: Int, analysisGroup: String?, sortBy: String?, sortDir: String?): ApiResult<PaginatedMedicalResults> =
            ApiResult.Success(PaginatedMedicalResults(items = latestResults, hasNext = false))
        override suspend fun getResultById(resultId: String): ApiResult<MedicalResult> =
            throw UnsupportedOperationException()
        override suspend fun getResultHistory(resultId: String): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getReportResults(reportId: String): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getReportDetails(reportId: String): ApiResult<ReportWithResults> =
            throw UnsupportedOperationException()
        override suspend fun getLatestResults(): ApiResult<List<MedicalResult>> =
            ApiResult.Success(latestResults)
        override suspend fun getResultsHistory(testDefinitionId: String): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getAiExplanation(reportId: String): ApiResult<String> =
            throw UnsupportedOperationException()
        override suspend fun getAiSummary(): ApiResult<AiSummaryResponse> =
            throw UnsupportedOperationException()
    }

    private class FakeGlobalSyncManager : GlobalSyncManager {
        private val _syncEvents = MutableSharedFlow<Unit>()
        override val syncEvents: SharedFlow<Unit> = _syncEvents
        override suspend fun triggerSync() { _syncEvents.emit(Unit) }
    }
}