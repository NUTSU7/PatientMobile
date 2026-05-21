package com.semanticsoft.patientmobile.data.remote.api

import com.semanticsoft.patientmobile.data.remote.api.dto.AiExplainRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.AiExplainResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.AiSummaryResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.AuthResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.BulkDeleteRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateMedicationRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateNoteRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateShareLinkRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.CreateSharedLinkDto
import com.semanticsoft.patientmobile.data.remote.api.dto.DashboardSummaryDto
import com.semanticsoft.patientmobile.data.remote.api.dto.DocumentDetailDto
import com.semanticsoft.patientmobile.data.remote.api.dto.DocumentDto
import com.semanticsoft.patientmobile.data.remote.api.dto.DuplicateCheckRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.DuplicateCheckResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.LoginRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.LogoutRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.MedicalResultDto
import com.semanticsoft.patientmobile.data.remote.api.dto.MedicalResultHistoryEntry
import com.semanticsoft.patientmobile.data.remote.api.dto.MedicationDto
import com.semanticsoft.patientmobile.data.remote.api.dto.OcrExtractionDto
import com.semanticsoft.patientmobile.data.remote.api.dto.PaginatedResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.PersonalNoteDto
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RenameDocumentRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.SharedLinkDto
import com.semanticsoft.patientmobile.data.remote.api.dto.StartExtractionResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.UpdateMedicationRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.UpdateNoteRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.UserDto
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

    // ── Auth ────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @GET("auth/me")
    suspend fun getCurrentUser(): UserDto

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
        @Query("dateTo") dateTo: String? = null
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

    @GET("patient/results")
    suspend fun getAllResults(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("analysisGroup") analysisGroup: String? = null
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

    // ── Dashboard ──────────────────────────────────────────────────────────

    @GET("patient/dashboard")
    suspend fun getDashboardSummary(): DashboardSummaryDto

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

    // ── Shared Links ───────────────────────────────────────────────────────

    @GET("shared/{token}")
    suspend fun consumeSharedLink(
        @Path("token") token: String,
        @Query("passphrase") passphrase: String? = null
    ): SharedLinkDto
}
