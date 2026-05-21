package com.semanticsoft.patientmobile.ui.shared.upload

import androidx.lifecycle.SavedStateHandle
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.ApiResult
import java.io.File
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UploadFileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onFilesSelected_addsFilesAsPending_noAutoUpload() = runTest {
        val temp = File.createTempFile("upload", ".pdf")
        temp.writeText("x")

        val vm = UploadFileViewModel(FakeDocumentRepository(), SavedStateHandle())

        vm.onFilesSelected(listOf(temp.absolutePath))

        assertEquals(1, vm.state.selectedFiles.size)
        assertEquals(UploadStatus.PENDING, vm.state.selectedFiles[0].status)
        assertFalse(vm.state.isUploading)
        assertFalse(vm.state.uploadComplete)
        temp.delete()
    }

    @Test
    fun startUpload_setsFileStatusToError_onFailure() = runTest {
        val temp = File.createTempFile("upload", ".pdf")
        temp.writeText("x")

        val vm = UploadFileViewModel(
            FakeDocumentRepository(uploadResult = ApiResult.HttpError(415, "Unsupported")),
            SavedStateHandle()
        )

        vm.onFilesSelected(listOf(temp.absolutePath))
        vm.startUpload()
        advanceUntilIdle()

        assertEquals(1, vm.state.selectedFiles.size)
        assertEquals(UploadStatus.ERROR, vm.state.selectedFiles[0].status)
        assertEquals("Format nesuportat (PDF, JPG, JPEG, PNG).", vm.state.selectedFiles[0].errorMessage)
        assertFalse(vm.state.isUploading)
        temp.delete()
    }

    @Test
    fun startUpload_marksUploadComplete_onAllSuccess() = runTest {
        val temp = File.createTempFile("upload", ".pdf")
        temp.writeText("x")

        val vm = UploadFileViewModel(FakeDocumentRepository(), SavedStateHandle())

        vm.onFilesSelected(listOf(temp.absolutePath))
        vm.startUpload()
        advanceUntilIdle()

        assertTrue(vm.state.uploadComplete)
        assertFalse(vm.state.isUploading)
        assertTrue(vm.state.selectedFiles.isEmpty())
        temp.delete()
    }

    @Test
    fun removeFile_removesFromList_whenNotUploading() = runTest {
        val temp = File.createTempFile("upload", ".pdf")
        temp.writeText("x")

        val vm = UploadFileViewModel(FakeDocumentRepository(), SavedStateHandle())

        vm.onFilesSelected(listOf(temp.absolutePath))

        vm.removeFile(0)

        assertTrue(vm.state.selectedFiles.isEmpty())
        temp.delete()
    }

    @Test
    fun retryFile_setsStatusToPending_thenTriggerUpload() = runTest {
        val temp = File.createTempFile("upload", ".pdf")
        temp.writeText("x")

        var callCount = 0
        val repo = object : DocumentRepository {
            override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> {
                callCount++
                if (callCount == 1) return ApiResult.HttpError(415, "bad")
                return ApiResult.Success(
                    PatientDocument(
                        id = "d1", originalFileName = file.name,
                        mimeType = "application/pdf", fileSizeBytes = file.length(),
                        uploadedAt = Instant.now()
                    )
                )
            }
            override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> =
                ApiResult.Success(emptyList())
            override suspend fun getDocuments(page: Int, size: Int): ApiResult<List<PatientDocument>> =
                throw UnsupportedOperationException()
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

        val vm = UploadFileViewModel(repo, SavedStateHandle())

        vm.onFilesSelected(listOf(temp.absolutePath))
        vm.startUpload()
        advanceUntilIdle()

        assertEquals(UploadStatus.ERROR, vm.state.selectedFiles[0].status)

        vm.retryFile(0)
        advanceUntilIdle()

        assertTrue(vm.state.selectedFiles.isEmpty())
        assertTrue(vm.state.uploadComplete)
        temp.delete()
    }

    private class FakeDocumentRepository(
        private val uploadResult: ApiResult<PatientDocument> = ApiResult.Success(
            PatientDocument(
                id = "d1",
                originalFileName = "test.pdf",
                mimeType = "application/pdf",
                fileSizeBytes = 100,
                uploadedAt = Instant.now()
            )
        )
    ) : DocumentRepository {
        override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> = uploadResult
        override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> =
            ApiResult.Success(emptyList())
        override suspend fun getDocuments(page: Int, size: Int): ApiResult<List<PatientDocument>> =
            ApiResult.Success(emptyList())
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
}