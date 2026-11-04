package com.semanticsoft.patientmobile.feature.patient.medicalHistory

import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.domain.model.common.AuthResponse
import com.semanticsoft.patientmobile.domain.model.common.RegistrationRequest
import com.semanticsoft.patientmobile.domain.model.common.User
import com.semanticsoft.patientmobile.domain.model.common.UserRole
import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.domain.model.patient.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.patient.DocumentStats
import com.semanticsoft.patientmobile.domain.model.patient.DoseUnit
import com.semanticsoft.patientmobile.domain.model.patient.MedicalHistoryAttachment
import com.semanticsoft.patientmobile.domain.model.patient.MedicalReport
import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.domain.model.patient.Medication
import com.semanticsoft.patientmobile.domain.model.patient.MedicationOcrDraft
import com.semanticsoft.patientmobile.domain.model.patient.OcrExtraction
import com.semanticsoft.patientmobile.domain.model.patient.PaginatedMedicalResults
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import com.semanticsoft.patientmobile.domain.model.patient.PersonalNote
import com.semanticsoft.patientmobile.domain.model.patient.PersonalNoteOcrDraft
import com.semanticsoft.patientmobile.domain.model.patient.ReportWithResults
import com.semanticsoft.patientmobile.domain.model.patient.SharedLink
import com.semanticsoft.patientmobile.domain.model.patient.Treatment
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.common.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalHistoryRepository
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.patient.OcrRepository
import com.semanticsoft.patientmobile.domain.usecase.patient.BuildTreatmentUseCase
import com.semanticsoft.patientmobile.testutil.MainDispatcherRule
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicalHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun register(request: RegistrationRequest): ApiResult<AuthResponse> =
            throw UnsupportedOperationException()
        override suspend fun refresh(): ApiResult<AuthResponse> = throw UnsupportedOperationException()
        override suspend fun logout() = Unit
        override suspend fun getCurrentUser(): ApiResult<User> =
            ApiResult.Success(User("u1", "test@example.com", "Test", "User", LocalDate.parse("1990-01-01"), UserRole.PATIENT, Instant.EPOCH))
        override suspend fun isLoggedIn(): Boolean = true
        override suspend fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun deleteAccount(password: String, confirmation: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun requestPasswordReset(email: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
        override suspend fun resetPassword(email: String, code: String, newPassword: String, confirmNewPassword: String): ApiResult<Unit> =
            throw UnsupportedOperationException()
    }

    private class FakeDocumentRepository : DocumentRepository {
        override suspend fun uploadDocument(file: File, force: Boolean): ApiResult<PatientDocument> =
            throw UnsupportedOperationException()
        override suspend fun checkDuplicates(checksums: List<String>): ApiResult<List<DocumentDuplicateInfo>> =
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
        override suspend fun createShareLink(id: String): ApiResult<SharedLink> =
            throw UnsupportedOperationException()
        override suspend fun getDocumentStats(): ApiResult<DocumentStats> =
            ApiResult.Success(DocumentStats(0, null))
    }

    private class FakeMedicalResultRepository : MedicalResultRepository {
        override suspend fun getByDocumentId(docId: String): ApiResult<List<MedicalResult>> =
            ApiResult.Success(emptyList())
        override suspend fun getLatestReport(documentId: String): ApiResult<MedicalReport> =
            throw UnsupportedOperationException()
        override suspend fun getAllResults(page: Int, size: Int, analysisGroup: String?, sortBy: String?, sortDir: String?): ApiResult<List<MedicalResult>> =
            ApiResult.Success(emptyList())
        override suspend fun getAllResultsPaginated(page: Int, size: Int, analysisGroup: String?, sortBy: String?, sortDir: String?): ApiResult<PaginatedMedicalResults> =
            ApiResult.Success(PaginatedMedicalResults(emptyList(), false))
        override suspend fun getResultById(resultId: String): ApiResult<MedicalResult> =
            throw UnsupportedOperationException()
        override suspend fun getResultHistory(resultId: String): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getReportResults(reportId: String): ApiResult<List<MedicalResult>> =
            throw UnsupportedOperationException()
        override suspend fun getReportDetails(reportId: String): ApiResult<ReportWithResults> =
            throw UnsupportedOperationException()
        override suspend fun getLatestResults(): ApiResult<List<MedicalResult>> =
            ApiResult.Success(emptyList())
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

    private class FakeMedicalHistoryRepository : MedicalHistoryRepository {
        override suspend fun createMedication(medication: Medication) = ApiResult.Success(medication)
        override suspend fun getMedications() = ApiResult.Success(listOf(
            Medication(
                id = "med-1",
                name = "Augmentin",
                treatmentId = "treat-1",
                treatmentName = "Tratament 1",
                doseValue = 1.0,
                doseUnit = DoseUnit.TABLET
            )
        ))
        override suspend fun updateMedication(id: String, medication: Medication) = ApiResult.Success(medication)
        override suspend fun deleteMedication(id: String) = ApiResult.Success(Unit)
        override suspend fun createTreatment(treatment: Treatment) = ApiResult.Success(treatment.medications)
        override suspend fun updateTreatment(id: String, treatment: Treatment) = ApiResult.Success(treatment.medications)
        override suspend fun deleteTreatment(id: String) = ApiResult.Success(Unit)
        override suspend fun createNote(note: PersonalNote) = ApiResult.Success(note)
        override suspend fun getNotes() = ApiResult.Success(emptyList<PersonalNote>())
        override suspend fun updateNote(id: String, note: PersonalNote) = ApiResult.Success(note)
        override suspend fun deleteNote(id: String) = ApiResult.Success(Unit)
        override suspend fun extractMedicationOcrDraft(filePath: String) = ApiResult.Success(
            MedicationOcrDraft(MedicalHistoryAttachment("1", "ocr.jpg", "image/jpeg", 100))
        )
        override suspend fun extractPersonalNoteOcrDraft(filePath: String) = ApiResult.Success(
            PersonalNoteOcrDraft(MedicalHistoryAttachment("1", "ocr.jpg", "image/jpeg", 100))
        )
    }

    private class FakeOcrRepository : OcrRepository {
        override suspend fun startExtraction(documentId: String): ApiResult<String> =
            throw UnsupportedOperationException()
        override suspend fun getExtractionStatus(documentId: String, runId: String): ApiResult<OcrExtraction> =
            throw UnsupportedOperationException()
        override suspend fun listExtractions(documentId: String): ApiResult<List<OcrExtraction>> =
            ApiResult.Success(emptyList())
        override fun pollOcrStatus(documentId: String, runId: String): Flow<ApiResult<OcrExtraction>> =
            throw UnsupportedOperationException()
    }

    private class FakeGlobalSyncManager : GlobalSyncManager {
        private val _syncEvents = MutableSharedFlow<Unit>()
        override val syncEvents: SharedFlow<Unit> = _syncEvents
        override suspend fun triggerSync() { _syncEvents.emit(Unit) }
    }

    private fun createViewModel() = MedicalHistoryViewModel(
        authRepository = FakeAuthRepository(),
        documentRepository = FakeDocumentRepository(),
        medicalResultRepository = FakeMedicalResultRepository(),
        medicalHistoryRepository = FakeMedicalHistoryRepository(),
        ocrRepository = FakeOcrRepository(),
        globalSyncManager = FakeGlobalSyncManager(),
        buildTreatment = BuildTreatmentUseCase()
    )

    @Test
    fun `onShowAddMedicationDialog seeds 1 draft and opens it`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onShowAddMedicationDialog()

        val state = vm.state.value
        assertEquals("add", state.medicationDialogTargetId)
        assertEquals(1, state.medicationDrafts.size)
        val draft = state.medicationDrafts.first()
        assertEquals(draft.draftId, state.expandedMedicationDraftId)
    }

    @Test
    fun `onAddMedicationDraft closes previous card and opens the new one`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onShowAddMedicationDialog()
        val firstDraftId = vm.state.value.expandedMedicationDraftId
        assertNotNull(firstDraftId)

        vm.onAddMedicationDraft()

        val state = vm.state.value
        assertEquals(2, state.medicationDrafts.size)
        val secondDraft = state.medicationDrafts.last()
        assertEquals(secondDraft.draftId, state.expandedMedicationDraftId)
        assertTrue(firstDraftId != secondDraft.draftId)
    }

    @Test
    fun `onToggleMedicationDraft toggles expansion`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onShowAddMedicationDialog()
        val draftId = vm.state.value.medicationDrafts.first().draftId

        // Clicking the already-open draft collapses it
        vm.onToggleMedicationDraft(draftId)
        assertNull(vm.state.value.expandedMedicationDraftId)

        // Clicking it again re-expands it
        vm.onToggleMedicationDraft(draftId)
        assertEquals(draftId, vm.state.value.expandedMedicationDraftId)
    }

    @Test
    fun `onShowEditMedicationDialog starts with all cards collapsed`() = runTest {
        val vm = createViewModel()
        vm.refresh()
        advanceUntilIdle()

        vm.onShowEditMedicationDialog("med-1")

        val state = vm.state.value
        assertEquals("treat-1", state.medicationDialogTargetId)
        assertEquals(1, state.medicationDrafts.size)
        assertNull(state.expandedMedicationDraftId)

        // Tapping card expands it
        val draftId = state.medicationDrafts.first().draftId
        vm.onToggleMedicationDraft(draftId)
        assertEquals(draftId, vm.state.value.expandedMedicationDraftId)
    }

    @Test
    fun `onDismissAddMedicationDialog clears expansion and drafts`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onShowAddMedicationDialog()
        vm.onDismissAddMedicationDialog()

        val state = vm.state.value
        assertNull(state.medicationDialogTargetId)
        assertNull(state.expandedMedicationDraftId)
        assertTrue(state.medicationDrafts.isEmpty())
    }
}
