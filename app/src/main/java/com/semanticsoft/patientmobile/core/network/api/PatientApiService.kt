package com.semanticsoft.patientmobile.core.network.api

import com.semanticsoft.patientmobile.core.network.dto.patient.AiExplainRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.AiExplainResponse
import com.semanticsoft.patientmobile.core.network.dto.patient.AiSummaryResponse
import com.semanticsoft.patientmobile.core.network.dto.patient.BulkDeleteRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.CreateMedicationRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.CreateNoteRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.CreateShareLinkRequest
import com.semanticsoft.patientmobile.core.network.dto.shared.CreateSharedLinkDto
import com.semanticsoft.patientmobile.core.network.dto.patient.DocumentDetailDto
import com.semanticsoft.patientmobile.core.network.dto.patient.DocumentDto
import com.semanticsoft.patientmobile.core.network.dto.patient.DuplicateCheckRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.DuplicateCheckResponse
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicalResultDto
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicalResultHistoryEntry
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicalReportWithResultsDto
import com.semanticsoft.patientmobile.core.network.dto.patient.UpdateMedicalReportReviewRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicationDto
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicationOcrDraftResponseDto
import com.semanticsoft.patientmobile.core.network.dto.patient.OcrExtractionDto
import com.semanticsoft.patientmobile.core.network.dto.shared.PaginatedResponse
import com.semanticsoft.patientmobile.core.network.dto.patient.PersonalNoteDto
import com.semanticsoft.patientmobile.core.network.dto.patient.PersonalNoteOcrDraftResponseDto
import com.semanticsoft.patientmobile.core.network.dto.patient.RenameDocumentRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.StartExtractionResponse
import com.semanticsoft.patientmobile.core.network.dto.patient.UpdateMedicationRequest
import com.semanticsoft.patientmobile.core.network.dto.patient.UpdateNoteRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PatientApiService {

    // ── Documents ──────────────────────────────────────────────────────────

    @Multipart
    @POST("patient/documents")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("force") force: RequestBody? = null
    ): DocumentDto

    @POST("patient/documents/duplicate-check")
    suspend fun checkDuplicates(@Body request: DuplicateCheckRequest): DuplicateCheckResponse

    @POST("patient/documents/bulk-delete")
    suspend fun bulkDeleteDocuments(@Body request: BulkDeleteRequest)

    @GET("patient/documents")
    suspend fun getDocuments(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortDir") sortDir: String? = null
    ): PaginatedResponse<DocumentDto>

    @GET("patient/documents/{documentId}")
    suspend fun getDocumentById(@Path("documentId") id: String): DocumentDetailDto

    @GET("patient/documents/{documentId}/file")
    suspend fun downloadDocument(@Path("documentId") id: String): Response<ResponseBody>

    @PATCH("patient/documents/{documentId}")
    suspend fun renameDocument(
        @Path("documentId") id: String,
        @Body request: RenameDocumentRequest
    ): DocumentDto

    @DELETE("patient/documents/{documentId}")
    suspend fun deleteDocument(@Path("documentId") id: String)

    @POST("patient/documents/{documentId}/share")
    suspend fun shareDocument(
        @Path("documentId") id: String,
        @Body request: CreateShareLinkRequest? = null
    ): CreateSharedLinkDto

    // ── OCR ────────────────────────────────────────────────────────────────

    @POST("patient/documents/{documentId}/extractions")
    suspend fun startExtraction(@Path("documentId") documentId: String): StartExtractionResponse

    @GET("patient/documents/{documentId}/extractions/{runId}")
    suspend fun getExtractionStatus(
        @Path("documentId") documentId: String,
        @Path("runId") runId: String
    ): OcrExtractionDto

    @GET("patient/documents/{documentId}/extractions")
    suspend fun listExtractions(@Path("documentId") documentId: String): List<OcrExtractionDto>

    // ── Medical Results ────────────────────────────────────────────────────

    @GET("patient/documents/{documentId}/results")
    suspend fun getMedicalResultsByDocument(@Path("documentId") documentId: String): List<MedicalResultDto>

    @GET("patient/medical/results")
    suspend fun getAllResults(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("analysisGroup") analysisGroup: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortDir") sortDir: String? = null
    ): PaginatedResponse<MedicalResultDto>

    @GET("patient/results/{resultId}")
    suspend fun getResultById(@Path("resultId") resultId: String): MedicalResultDto

    @GET("patient/results/{resultId}/history")
    suspend fun getResultHistory(@Path("resultId") resultId: String): List<MedicalResultHistoryEntry>

    @GET("patient/medical/results/latest")
    suspend fun getLatestResults(): List<MedicalResultDto>

    @GET("patient/medical/results/history")
    suspend fun getResultsHistoryByTestDefinition(
        @Query("testDefinitionId") testDefinitionId: String
    ): List<MedicalResultDto>

    @GET("patient/documents/{documentId}/reports")
    suspend fun getReportsByDocument(@Path("documentId") documentId: String): List<OcrExtractionDto>

    @GET("patient/medical/reports/{reportId}/results")
    suspend fun getReportResults(
        @Path("reportId") reportId: String
    ): MedicalReportWithResultsDto

    @PATCH("patient/medical/reports/{reportId}")
    suspend fun updateMedicalReport(
        @Path("reportId") reportId: String,
        @Body request: UpdateMedicalReportReviewRequest
    ): MedicalReportWithResultsDto

    // ── Dashboard ──────────────────────────────────────────────────────────

    @GET("patient/dashboard/ai-summary")
    suspend fun getAiSummary(): AiSummaryResponse

    @POST("patient/dashboard/ai-summary/regenerate")
    suspend fun regenerateAiSummary(): AiSummaryResponse

    @POST("patient/dashboard/summary")
    suspend fun requestAiExplanation(@Body request: AiExplainRequest): AiExplainResponse

    @POST("patient/medical/reports/{reportId}/ai-explanation")
    suspend fun getAiExplanation(
        @Path("reportId") reportId: String
    ): AiExplainResponse

    // ── Medical History ────────────────────────────────────────────────────

    @POST("patient/medical-history/medications")
    suspend fun createMedication(@Body request: CreateMedicationRequest): MedicationDto

    @GET("patient/medical-history/medications")
    suspend fun getMedications(): List<MedicationDto>

    @PUT("patient/medical-history/medications/{medicationId}")
    suspend fun updateMedication(
        @Path("medicationId") id: String,
        @Body request: UpdateMedicationRequest
    ): MedicationDto

    @DELETE("patient/medical-history/medications/{medicationId}")
    suspend fun deleteMedication(@Path("medicationId") id: String)

    @POST("patient/medical-history/personal-notes")
    suspend fun createNote(@Body request: CreateNoteRequest): PersonalNoteDto

    @GET("patient/medical-history/personal-notes")
    suspend fun getNotes(): List<PersonalNoteDto>

    @PUT("patient/medical-history/personal-notes/{noteId}")
    suspend fun updateNote(
        @Path("noteId") id: String,
        @Body request: UpdateNoteRequest
    ): PersonalNoteDto

    @DELETE("patient/medical-history/personal-notes/{noteId}")
    suspend fun deleteNote(@Path("noteId") id: String)

    @Multipart
    @POST("patient/medical-history/medications/ocr-drafts")
    suspend fun extractMedicationOcrDraft(
        @Part file: MultipartBody.Part
    ): MedicationOcrDraftResponseDto

    @Multipart
    @POST("patient/medical-history/personal-notes/ocr-drafts")
    suspend fun extractPersonalNoteOcrDraft(
        @Part file: MultipartBody.Part
    ): PersonalNoteOcrDraftResponseDto
}
