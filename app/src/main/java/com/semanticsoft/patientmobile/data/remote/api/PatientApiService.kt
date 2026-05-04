package com.semanticsoft.patientmobile.data.remote.api

import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.data.remote.api.dto.AuthResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.LoginRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.DuplicateCheckRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.DuplicateCheckResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.PaginatedResponse
import com.semanticsoft.patientmobile.data.remote.api.dto.RefreshRequest
import com.semanticsoft.patientmobile.data.remote.api.dto.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PatientApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ResponseEntity<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Body refreshToken: String): Unit

    @GET("auth/me")
    suspend fun getCurrentUser(): User

    @Multipart
    @POST("patient/documents")
    suspend fun uploadDocument(@Part file: MultipartBody.Part): PatientDocument

    @GET("patient/documents")
    suspend fun getDocuments(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PaginatedResponse<PatientDocument>

    @GET("patient/documents/{documentId}")
    suspend fun getDocumentById(@Path("documentId") id: String): PatientDocument

    @GET("patient/documents/{documentId}/file")
    suspend fun downloadDocument(@Path("documentId") id: String): ResponseBody

    @POST("patient/documents/duplicate-check")
    suspend fun checkDuplicates(@Body request: DuplicateCheckRequest): DuplicateCheckResponse

    @GET("patient/documents/{documentId}/results")
    suspend fun getMedicalResults(@Path("documentId") documentId: String): List<MedicalResult>
}
