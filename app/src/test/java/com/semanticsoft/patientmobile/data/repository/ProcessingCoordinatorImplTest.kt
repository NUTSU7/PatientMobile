package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.repository.shared.ProcessingCoordinatorImpl
import com.semanticsoft.patientmobile.domain.model.DocumentStats
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.OcrExtraction
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.domain.model.PaginatedMedicalResults
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.ProcessingOutcome
import com.semanticsoft.patientmobile.domain.model.ReportWithResults
import com.semanticsoft.patientmobile.domain.repository.patient.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.ExtractionTarget
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.OcrRepository
import com.semanticsoft.patientmobile.util.ApiResult
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.AiSummaryResponse
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProcessingCoordinatorImplTest {

    @Test
    fun start_allLabSuccess_marksCompleted_andRegeneratesAi() = runTest {
        val ocr = FakeOcrRepository(
            status = OcrExtraction(
                id = "r1", documentId = "d1", engineName = "tesseract",
                status = OcrStatus.SUCCESS,
                reports = listOf(MedicalReport(id = "rep1", title = "Blood", clinicalType = "LAB_RESULTS"))
            )
        )
        val dashboard = FakeDashboardRepository()
        val coordinator = ProcessingCoordinatorImpl(
            ocrRepository = ocr,
            documentRepository = FakeDocumentRepository(),
            dashboardRepository = dashboard,
            globalSyncManager = FakeGlobalSyncManager(),
            scope = this
        )

        coordinator.start(listOf(ExtractionTarget("d1", "r1")))
        advanceUntilIdle()

        assertFalse(coordinator.status.value.isProcessing)
        assertEquals(ProcessingOutcome.COMPLETED, coordinator.status.value.lastOutcome)
        assertEquals(1, coordinator.status.value.completedJobs)
        assertTrue(dashboard.regenerateCalled)
    }

    @Test
    fun start_nonLabSuccess_marksNonLabOutcome() = runTest {
        val ocr = FakeOcrRepository(
            status = OcrExtraction(
                id = "r1", documentId = "d1", engineName = "tesseract",
                status = OcrStatus.SUCCESS,
                reports = listOf(MedicalReport(id = "rep1", title = "Letter", clinicalType = "MEDICAL_LETTER"))
            )
        )
        val coordinator = ProcessingCoordinatorImpl(
            ocrRepository = ocr,
            documentRepository = FakeDocumentRepository(),
            dashboardRepository = FakeDashboardRepository(),
            globalSyncManager = FakeGlobalSyncManager(),
            scope = this
        )

        coordinator.start(listOf(ExtractionTarget("d1", "r1")))
        advanceUntilIdle()

        assertEquals(ProcessingOutcome.NON_LAB, coordinator.status.value.lastOutcome)
    }

    @Test
    fun start_failure_marksNoResults_andDoesNotRegenerateAi() = runTest {
        val ocr = FakeOcrRepository(
            status = OcrExtraction(
                id = "r1", documentId = "d1", engineName = "tesseract",
                status = OcrStatus.FAILED,
                reports = emptyList()
            )
        )
        val dashboard = FakeDashboardRepository()
        val coordinator = ProcessingCoordinatorImpl(
            ocrRepository = ocr,
            documentRepository = FakeDocumentRepository(),
            dashboardRepository = dashboard,
            globalSyncManager = FakeGlobalSyncManager(),
            scope = this
        )

        coordinator.start(listOf(ExtractionTarget("d1", "r1")))
        advanceUntilIdle()

        assertEquals(ProcessingOutcome.NO_RESULTS, coordinator.status.value.lastOutcome)
        assertEquals(1, coordinator.status.value.failedJobs)
        assertFalse(dashboard.regenerateCalled)
    }

    private class FakeOcrRepository(
        private val status: OcrExtraction
    ) : OcrRepository {
        override suspend fun startExtraction(documentId: String): ApiResult<String> =
            ApiResult.Success("r1")
        override suspend fun getExtractionStatus(documentId: String, runId: String): ApiResult<OcrExtraction> =
            ApiResult.Success(status)
        override suspend fun listExtractions(documentId: String): ApiResult<List<OcrExtraction>> =
            ApiResult.Success(emptyList())
        override fun pollOcrStatus(documentId: String, runId: String): Flow<ApiResult<OcrExtraction>> =
            throw UnsupportedOperationException()
    }

    private class FakeDocumentRepository : DocumentRepository {
        override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo>> =
            throw UnsupportedOperationException()
        override suspend fun getDocuments(
            page: Int, size: Int, search: String?, dateFrom: String?, dateTo: String?,
            sortBy: String?, sortDir: String?
        ): ApiResult<List<PatientDocument>> = ApiResult.Success(emptyList())
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
        override suspend fun getDocumentStats(): ApiResult<DocumentStats> =
            ApiResult.Success(DocumentStats(1, null))
    }

    private class FakeDashboardRepository : DashboardRepository {
        var regenerateCalled = false
        override suspend fun getAiSummary(): ApiResult<AiSummaryResponse> =
            ApiResult.Success(AiSummaryResponse("", "STALE"))
        override suspend fun regenerateAiSummary(): ApiResult<AiSummaryResponse> {
            regenerateCalled = true
            return ApiResult.Success(AiSummaryResponse("summary", "READY"))
        }
        override suspend fun requestExplanation(reportId: String): ApiResult<String> =
            throw UnsupportedOperationException()
    }

    private class FakeGlobalSyncManager : GlobalSyncManager {
        private val _syncEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        override val syncEvents: SharedFlow<Unit> = _syncEvents
        override suspend fun triggerSync() { _syncEvents.emit(Unit) }
    }
}
