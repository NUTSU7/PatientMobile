package com.semanticsoft.patientmobile.ui.screens.uploadFile

import androidx.lifecycle.SavedStateHandle
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
    fun uploadFile_setsError_whenFileNotSelected() = runTest {
        val vm = UploadFileViewModel(FakeDocumentRepository(), SavedStateHandle())

        vm.uploadFile(UploadSource.FILE_PICKER)

        assertEquals("Selectează un fișier înainte de upload.", vm.state.errorMessage)
    }

    @Test
    fun uploadFile_mapsUnsupportedTypeError() = runTest {
        val temp = File.createTempFile("upload", ".pdf")
        temp.writeText("x")

        val vm = UploadFileViewModel(
            FakeDocumentRepository(throwOnUpload = UnsupportedMediaTypeException("bad")),
            SavedStateHandle()
        )
        vm.onFileSelected(temp.absolutePath)

        vm.uploadFile(UploadSource.FILE_PICKER)
        advanceUntilIdle()

        assertEquals("Format nesuportat (pdf, jpg, jpeg, png).", vm.state.errorMessage)
        assertFalse(vm.state.isUploading)
        temp.delete()
    }

    @Test
    fun uploadFile_success_resetsSelection() = runTest {
        val temp = File.createTempFile("upload", ".pdf")
        temp.writeText("x")

        val vm = UploadFileViewModel(FakeDocumentRepository(), SavedStateHandle())
        vm.onFileSelected(temp.absolutePath)

        vm.uploadFile(UploadSource.FILE_PICKER)
        advanceUntilIdle()

        assertTrue(vm.state.selectedFileName == null)
        assertFalse(vm.state.isUploading)
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

        override fun getDocuments(): Flow<Resource<List<PatientDocument>>> = throw UnsupportedOperationException()
        override fun getDocumentById(id: String): Flow<Resource<PatientDocument>> = throw UnsupportedOperationException()
        override suspend fun downloadDocument(id: String): File = throw UnsupportedOperationException()
    }
}
