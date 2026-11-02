package com.semanticsoft.patientmobile.core.network.api

import com.semanticsoft.patientmobile.core.network.dto.auth.AuthResponse
import com.semanticsoft.patientmobile.core.network.dto.auth.ChangePasswordRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.DeleteAccountRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.ForgotPasswordRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.LoginRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.LogoutRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.RefreshRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.RefreshResponse
import com.semanticsoft.patientmobile.core.network.dto.auth.RegisterRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.ResetPasswordRequest
import com.semanticsoft.patientmobile.core.network.dto.auth.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface AuthApiService {

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

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest)

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest)

    @HTTP(method = "DELETE", path = "auth/account", hasBody = true)
    suspend fun deleteAccount(@Body request: DeleteAccountRequest)
}
