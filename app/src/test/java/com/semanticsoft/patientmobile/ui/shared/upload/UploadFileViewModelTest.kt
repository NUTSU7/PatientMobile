package com.semanticsoft.patientmobile.ui.shared.upload

import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.OcrRepository
import com.semanticsoft.patientmobile.util.ApiResult
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UploadFileViewModelTest {

    @Test
    fun onFilesSelected_addsFilesAsPending_noAutoUpload() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val temp = File.createTempFile("upload", ".pdf")
            temp.writeText("x")
            val vm = UploadFileViewModel(
                FakeDocumentRepository(), FakeGlobalSyncManager(), FakeOcrRepository(),
                StandardTestDispatcher(testScheduler)
            )
            vm.onFilesSelected(listOf(temp.absolutePath))
            assertEquals(1, vm.state.value.selectedFiles.size)
            assertEquals(UploadStatus.PENDING, vm.state.value.selectedFiles[0].status)
            temp.delete()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun startUpload_setsFileStatusToError_onFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val temp = File.createTempFile("upload", ".pdf")
            temp.writeText("x")
            val vm = UploadFileViewModel(
                FakeDocumentRepository(uploadResult = ApiResult.HttpError(415, "Unsupported")),
                FakeGlobalSyncManager(), FakeOcrRepository(),
                StandardTestDispatcher(testScheduler)
            )
            vm.onFilesSelected(listOf(temp.absolutePath))
            vm.startUpload()
            advanceUntilIdle()
            assertEquals(UploadStatus.ERROR, vm.state.value.selectedFiles[0].status)
            assertEquals("Format nesuportat (PDF, JPG, JPEG, PNG).", vm.state.value.selectedFiles[0].errorMessage)
            temp.delete()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun startUpload_marksUploadComplete_onAllSuccess() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val temp = File.createTempFile("upload", ".pdf")
            temp.writeText("x")
            val vm = UploadFileViewModel(
                FakeDocumentRepository(), FakeGlobalSyncManager(), FakeOcrRepository(),
                StandardTestDispatcher(testScheduler)
            )
            vm.onFilesSelected(listOf(temp.absolutePath))
            vm.startUpload()
            advanceUntilIdle()
            assertTrue(vm.state.value.uploadComplete)
            assertTrue(vm.state.value.selectedFiles.isEmpty())
            temp.delete()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun removeFile_removesFromList_whenNotUploading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val temp = File.createTempFile("upload", ".pdf")
            temp.writeText("x")
            val vm = UploadFileViewModel(
                FakeDocumentRepository(), FakeGlobalSyncManager(), FakeOcrRepository(),
                StandardTestDispatcher(testScheduler)
            )
            vm.onFilesSelected(listOf(temp.absolutePath))
            vm.removeFile(0)
            assertTrue(vm.state.value.selectedFiles.isEmpty())
            temp.delete()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun retryFile_setsStatusToPending_thenTriggerUpload() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val temp = File.createTempFile("upload", ".pdf")
            temp.writeText("x")
            var callCount = 0
            val repo = object : DocumentRepository {
                override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> {
                    callCount++
                    if (callCount == 1) return ApiResult.HttpError(415, "bad")
                    return ApiResult.Success(PatientDocument("d1", file.name, "application/pdf", file.length(), Instant.now()))
                }
                override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> = ApiResult.Success(emptyList())
                override suspend fun getDocuments(p: Int, s: Int, search: String?, dF: String?, dT: String?, sortB: String?, sortD: String?): ApiResult<List<PatientDocument>> = throw UnsupportedOperationException()
                override suspend fun getDocumentById(id: String) = throw UnsupportedOperationException()
                override suspend fun downloadDocument(id: String) = throw UnsupportedOperationException()
                override suspend fun downloadDocumentFile(documentId: String) = throw UnsupportedOperationException()
                override suspend fun getDocumentExplanation(documentId: String) = throw UnsupportedOperationException()
                override suspend fun renameDocument(id: String, newName: String) = throw UnsupportedOperationException()
                override suspend fun deleteDocument(id: String) = throw UnsupportedOperationException()
                override suspend fun bulkDelete(documentIds: List<String>) = throw UnsupportedOperationException()
                override suspend fun createShareLink(id: String) = throw UnsupportedOperationException()
                override suspend fun getDocumentStats() = throw UnsupportedOperationException()
            }
            val vm = UploadFileViewModel(repo, FakeGlobalSyncManager(), FakeOcrRepository(), StandardTestDispatcher(testScheduler))
            vm.onFilesSelected(listOf(temp.absolutePath))
            vm.startUpload()
            advanceUntilIdle()
            assertEquals(UploadStatus.ERROR, vm.state.value.selectedFiles[0].status)
            vm.retryFile(0)
            advanceUntilIdle()
            assertTrue(vm.state.value.selectedFiles.isEmpty())
            assertTrue(vm.state.value.uploadComplete)
            temp.delete()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun startUpload_triggersExtraction_afterSuccessfulUpload() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val documentId = "doc-123"
            val runId = "run-456"
            val temp = File.createTempFile("upload", ".pdf")
            temp.writeText("x")
            val vm = UploadFileViewModel(
                FakeDocumentRepository(uploadResult = ApiResult.Success(PatientDocument(documentId, temp.name, "application/pdf", temp.length(), Instant.now()))),
                FakeGlobalSyncManager(),
                FakeOcrRepository(startExtractionResult = ApiResult.Success(runId)),
                StandardTestDispatcher(testScheduler)
            )
            vm.onFilesSelected(listOf(temp.absolutePath))
            vm.startUpload()
            advanceUntilIdle()
            assertTrue(vm.state.value.uploadComplete)
            assertTrue(vm.state.value.selectedFiles.isEmpty())
            assertEquals(1, vm.state.value.extractionJobs.size)
            assertEquals(documentId, vm.state.value.extractionJobs[0].documentId)
            assertEquals(runId, vm.state.value.extractionJobs[0].runId)
            temp.delete()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun startUpload_succeeds_whenExtractionFails() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val documentId = "doc-123"
            val temp = File.createTempFile("upload", ".pdf")
            temp.writeText("x")
            val vm = UploadFileViewModel(
                FakeDocumentRepository(uploadResult = ApiResult.Success(PatientDocument(documentId, temp.name, "application/pdf", temp.length(), Instant.now()))),
                FakeGlobalSyncManager(),
                FakeOcrRepository(startExtractionResult = ApiResult.HttpError(503, "Service unavailable")),
                StandardTestDispatcher(testScheduler)
            )
            vm.onFilesSelected(listOf(temp.absolutePath))
            vm.startUpload()
            advanceUntilIdle()
            assertTrue(vm.state.value.uploadComplete)
            assertTrue(vm.state.value.selectedFiles.isEmpty())
            assertEquals(0, vm.state.value.extractionJobs.size)
            temp.delete()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeDocumentRepository(
        private val uploadResult: ApiResult<PatientDocument> = ApiResult.Success(PatientDocument("d1", "test.pdf", "application/pdf", 100, Instant.now()))
    ) : DocumentRepository {
        override suspend fun uploadDocument(file: File, force: Boolean) = uploadResult
        override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> = ApiResult.Success(emptyList())
        override suspend fun getDocuments(p: Int, s: Int, search: String?, dF: String?, dT: String?, sortB: String?, sortD: String?): ApiResult<List<PatientDocument>> = ApiResult.Success(emptyList())
        override suspend fun getDocumentById(id: String) = throw UnsupportedOperationException()
        override suspend fun downloadDocument(id: String) = throw UnsupportedOperationException()
        override suspend fun downloadDocumentFile(documentId: String) = throw UnsupportedOperationException()
        override suspend fun getDocumentExplanation(documentId: String) = throw UnsupportedOperationException()
        override suspend fun renameDocument(id: String, newName: String) = throw UnsupportedOperationException()
        override suspend fun deleteDocument(id: String) = throw UnsupportedOperationException()
        override suspend fun bulkDelete(documentIds: List<String>) = throw UnsupportedOperationException()
        override suspend fun createShareLink(id: String) = throw UnsupportedOperationException()
        override suspend fun getDocumentStats() = throw UnsupportedOperationException()
    }

    private class FakeGlobalSyncManager : GlobalSyncManager {
        private val _syncEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        override val syncEvents: SharedFlow<Unit> = _syncEvents
        override suspend fun triggerSync() { _syncEvents.emit(Unit) }
    }

    private class FakeOcrRepository(
        private val startExtractionResult: ApiResult<String> = ApiResult.Success("run-456")
    ) : OcrRepository {
        override suspend fun startExtraction(documentId: String) = startExtractionResult
        override suspend fun getExtractionStatus(documentId: String, runId: String) = throw UnsupportedOperationException()
        override suspend fun listExtractions(documentId: String) = throw UnsupportedOperationException()
        override fun pollOcrStatus(documentId: String, runId: String) = throw UnsupportedOperationException()
    }
}
