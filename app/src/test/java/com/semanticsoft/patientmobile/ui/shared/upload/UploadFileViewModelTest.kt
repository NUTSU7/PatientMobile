package com.semanticsoft.patientmobile.ui.shared.upload

import androidx.lifecycle.SavedStateHandle
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.SyncStatus
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import com.semanticsoft.patientmobile.util.Resource
import com.semanticsoft.patientmobile.util.exceptions.UnsupportedMediaTypeException
import java.io.File
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
            FakeDocumentRepository(throwOnUpload = UnsupportedMediaTypeException("bad")),
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
            override suspend fun uploadDocument(file: File): PatientDocument {
                callCount++
                if (callCount == 1) throw UnsupportedMediaTypeException("bad")
                return PatientDocument(
                    id = "d1", ownerUserId = "u1", originalFileName = file.name,
                    mimeType = "application/pdf", fileSizeBytes = file.length(),
                    uploadedAt = Instant.now(), syncStatus = SyncStatus.SYNCED
                )
            }
            override suspend fun checkDuplicates(checksums: List<String>): List<DocumentDuplicateInfo> = emptyList()
            override fun getDocuments(): Flow<Resource<List<PatientDocument>>> = throw UnsupportedOperationException()
            override fun getDocumentById(id: String): Flow<Resource<PatientDocument>> = throw UnsupportedOperationException()
            override suspend fun downloadDocument(id: String): File = throw UnsupportedOperationException()
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
        private val throwOnUpload: Throwable? = null
    ) : DocumentRepository {
        override suspend fun uploadDocument(file: File): PatientDocument {
            throwOnUpload?.let { throw it }
            return PatientDocument(
                id = "d1",
                ownerUserId = "u1",
                originalFileName = file.name,
                mimeType = "application/pdf",
                fileSizeBytes = file.length(),
                uploadedAt = Instant.now(),
                syncStatus = SyncStatus.SYNCED
            )
        }

        override suspend fun checkDuplicates(checksums: List<String>): List<DocumentDuplicateInfo> = emptyList()

        override fun getDocuments(): Flow<Resource<List<PatientDocument>>> = throw UnsupportedOperationException()
        override fun getDocumentById(id: String): Flow<Resource<PatientDocument>> = throw UnsupportedOperationException()
        override suspend fun downloadDocument(id: String): File = throw UnsupportedOperationException()
    }
}
