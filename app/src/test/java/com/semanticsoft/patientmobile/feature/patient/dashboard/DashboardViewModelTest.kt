package com.semanticsoft.patientmobile.feature.patient.dashboard

import com.semanticsoft.patientmobile.domain.model.common.RegistrationRequest
import com.semanticsoft.patientmobile.domain.model.patient.PaginatedMedicalResults
import com.semanticsoft.patientmobile.domain.model.common.AuthResponse
import com.semanticsoft.patientmobile.domain.model.patient.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.patient.DocumentStats
import com.semanticsoft.patientmobile.domain.model.patient.MedicalReport
import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.domain.model.patient.OcrStatus
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import com.semanticsoft.patientmobile.domain.model.patient.ProcessingStatus
import com.semanticsoft.patientmobile.domain.model.patient.ReportWithResults
import com.semanticsoft.patientmobile.domain.model.common.User
import com.semanticsoft.patientmobile.domain.model.common.UserRole
import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.patient.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.patient.ExtractionTarget
import com.semanticsoft.patientmobile.domain.repository.common.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.patient.ProcessingCoordinator
import com.semanticsoft.patientmobile.domain.usecase.common.LogoutUseCase
import com.semanticsoft.patientmobile.domain.usecase.patient.BuildDashboardUseCase
import com.semanticsoft.patientmobile.domain.usecase.patient.LoadIndicatorHistoryUseCase
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.core.util.ApiResult
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refresh_noError_whenResultsAvailable() = runTest {
        val authRepository = FakeAuthRepository()
        val medicalResultRepository = FakeMedicalResultRepository()
        val vm = DashboardViewModel(
            authRepository = authRepository,
            dashboardRepository = FakeDashboardRepository(),
            medicalResultRepository = medicalResultRepository,
            documentRepository = FakeDocumentRepository(
                documentsResult = ApiResult.Success(
                    listOf(
                        PatientDocument(
                            id = "d1",
                            originalFileName = "lab.pdf",
                            mimeType = "application/pdf",
                            fileSizeBytes = 100,
                            uploadedAt = Instant.now()
                        )
                    )
                ),
                statsResult = ApiResult.Success(DocumentStats(totalCount = 1, lastUploadedAt = Instant.now()))
            ),
            globalSyncManager = FakeGlobalSyncManager(),
            processingCoordinator = FakeProcessingCoordinator(),
            buildDashboardUseCase = BuildDashboardUseCase(),
            loadIndicatorHistoryUseCase = LoadIndicatorHistoryUseCase(medicalResultRepository),
            logoutUseCase = LogoutUseCase(authRepository)
        )

        val collectJob = launch { vm.state.collect {} }
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        collectJob.cancel()

        assertFalse(vm.state.value.isLoading)
        assertFalse(vm.state.value.hasUploadedDocuments.not())
    }

    @Test
    fun refresh_setsError_whenAllSourcesFail() = runTest {
        val authRepository = FakeAuthRepository()
        val medicalResultRepository = FakeMedicalResultRepository(emptyResults = true)
        val vm = DashboardViewModel(
            authRepository = authRepository,
            dashboardRepository = FakeDashboardRepository(),
            medicalResultRepository = medicalResultRepository,
            documentRepository = FakeDocumentRepository(
                documentsResult = ApiResult.NetworkError,
                statsResult = ApiResult.NetworkError
            ),
            globalSyncManager = FakeGlobalSyncManager(),
            processingCoordinator = FakeProcessingCoordinator(),
            buildDashboardUseCase = BuildDashboardUseCase(),
            loadIndicatorHistoryUseCase = LoadIndicatorHistoryUseCase(medicalResultRepository),
            logoutUseCase = LogoutUseCase(authRepository)
        )

        val collectJob = launch { vm.state.collect {} }
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        collectJob.cancel()

        assertFalse(vm.state.value.isLoading)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun refresh_updatesSummary_whenDataAvailable() = runTest {
        val authRepository = FakeAuthRepository()
        val medicalResultRepository = FakeMedicalResultRepository()
        val vm = DashboardViewModel(
            authRepository = authRepository,
            dashboardRepository = FakeDashboardRepository(),
            medicalResultRepository = medicalResultRepository,
            documentRepository = FakeDocumentRepository(
                documentsResult = ApiResult.Success(
                    listOf(
                        PatientDocument(
                            id = "d1",
                            originalFileName = "lab.pdf",
                            mimeType = "application/pdf",
                            fileSizeBytes = 100,
                            uploadedAt = Instant.now()
                        )
                    )
                ),
                statsResult = ApiResult.Success(DocumentStats(totalCount = 1, lastUploadedAt = Instant.now()))
            ),
            globalSyncManager = FakeGlobalSyncManager(),
            processingCoordinator = FakeProcessingCoordinator(),
            buildDashboardUseCase = BuildDashboardUseCase(),
            loadIndicatorHistoryUseCase = LoadIndicatorHistoryUseCase(medicalResultRepository),
            logoutUseCase = LogoutUseCase(authRepository)
        )

        val collectJob = launch { vm.state.collect {} }
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        collectJob.cancel()

        assertTrue(vm.state.value.markerSummary.attention >= 0)
        assertFalse(vm.state.value.isLoading)
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun register(request: RegistrationRequest): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun refresh(): ApiResult<AuthResponse> = throw UnsupportedOperationException()
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): ApiResult<User> =
            ApiResult.Success(User("u1", "john@example.com", "John", "Doe", LocalDate.parse("1990-01-01"), UserRole.PATIENT, Instant.EPOCH))
        override suspend fun isLoggedIn(): Boolean = true
        override suspend fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun deleteAccount(password: String, confirmation: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
    }

    private class FakeDashboardRepository : DashboardRepository {
        override suspend fun getAiSummary(): ApiResult<AiSummary> =
            ApiResult.Success(AiSummary(summaryText = "Summary", status = "READY"))
        override suspend fun regenerateAiSummary(): ApiResult<AiSummary> =
            ApiResult.Success(AiSummary(summaryText = "Regenerated", status = "READY"))
        override suspend fun requestExplanation(reportId: String): ApiResult<String> =
            throw UnsupportedOperationException()
    }

    private class FakeDocumentRepository(
        private val documentsResult: ApiResult<List<PatientDocument>>,
        private val statsResult: ApiResult<DocumentStats> = ApiResult.Success(DocumentStats(0, null))
    ) : DocumentRepository {
        override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> =
            throw UnsupportedOperationException()
        override suspend fun getDocuments(
            page: Int, size: Int, search: String?, dateFrom: String?, dateTo: String?,
            sortBy: String?, sortDir: String?
        ): ApiResult<List<PatientDocument>> = documentsResult
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
        override suspend fun createShareLink(id: String): ApiResult<com.semanticsoft.patientmobile.domain.model.patient.SharedLink> =
            throw UnsupportedOperationException()
        override suspend fun getDocumentStats(): ApiResult<DocumentStats> = statsResult
    }

    private class FakeMedicalResultRepository(
        private val emptyResults: Boolean = false
    ) : MedicalResultRepository {
        private val latestResults: List<MedicalResult> = if (emptyResults) emptyList() else listOf(
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
        override suspend fun getAiSummary(): ApiResult<AiSummary> =
            throw UnsupportedOperationException()
        override suspend fun updateReport(
            reportId: String,
            observedAt: String?,
            displayTitle: String?,
            displaySubtitle: String?,
            sourceLaboratory: String?
        ): ApiResult<ReportWithResults> = throw UnsupportedOperationException()
    }

    private class FakeGlobalSyncManager : GlobalSyncManager {
        private val _syncEvents = MutableSharedFlow<Unit>()
        override val syncEvents: SharedFlow<Unit> = _syncEvents
        override suspend fun triggerSync() { _syncEvents.emit(Unit) }
    }

    private class FakeProcessingCoordinator : ProcessingCoordinator {
        private val _status = MutableStateFlow(ProcessingStatus())
        override val status: StateFlow<ProcessingStatus> = _status
        override fun start(targets: List<ExtractionTarget>) = Unit
        override fun resumeIfPending() = Unit
        override fun reset() = Unit
    }
}
