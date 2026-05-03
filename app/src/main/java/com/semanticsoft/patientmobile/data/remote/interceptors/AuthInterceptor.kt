package com.semanticsoft.patientmobile.data.remote.interceptors

import android.content.Context
import android.util.Log
import com.semanticsoft.patientmobile.BuildConfig
import com.semanticsoft.patientmobile.data.local.datastore.SecureTokenStore
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor(
    context: Context
) : Interceptor {
    private val tokenStore = SecureTokenStore(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authRequest = addBearerIfNeeded(originalRequest)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Request: ${authRequest.method} ${authRequest.url}")
        }

        val firstResponse = chain.proceed(authRequest)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Response: ${firstResponse.code} for ${authRequest.url}")
        }
        if (firstResponse.code != 401 || isAuthRefreshCall(originalRequest)) {
            return firstResponse
        }

        firstResponse.close()

        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken.isNullOrBlank() || !refreshAccessToken(originalRequest, refreshToken)) {
            tokenStore.clearTokens()
            return chain.proceed(originalRequest)
        }

        val retriedAccessToken = tokenStore.getAccessToken()
        val retriedRequest = if (retriedAccessToken.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header(AUTH_HEADER, "$BEARER $retriedAccessToken")
                .build()
        }

        return chain.proceed(retriedRequest)
    }

    private fun addBearerIfNeeded(request: Request): Request {
        if (isPublicAuthEndpoint(request) || request.header(AUTH_HEADER) != null) {
            return request
        }

        val token = tokenStore.getAccessToken().orEmpty()
        if (token.isBlank()) return request

        return request.newBuilder()
            .header(AUTH_HEADER, "$BEARER $token")
            .build()
    }

    private fun refreshAccessToken(failedRequest: Request, refreshToken: String): Boolean {
        val base = failedRequest.url
        val refreshUrl = base.newBuilder()
            .encodedPath("/api/v1/auth/refresh")
            .build()

        val payload = JSONObject().put("refreshToken", refreshToken).toString()
        val body = payload.toRequestBody("application/json; charset=utf-8".toMediaType())

        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .post(body)
            .build()

        val response = OkHttpClient.Builder().build().newCall(refreshRequest).execute()
        response.use {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Refresh response: ${it.code}")
            }
            if (!it.isSuccessful) return false
            val responseBody = it.body?.string().orEmpty()
            if (responseBody.isBlank()) return false

            val json = JSONObject(responseBody)
            val newAccessToken = json.optString("accessToken", "")
            val newRefreshToken = json.optString("refreshToken", "")
            if (newAccessToken.isBlank() || newRefreshToken.isBlank()) return false

            tokenStore.saveTokens(newAccessToken, newRefreshToken)
            return true
        }
    }

    private fun isAuthRefreshCall(request: Request): Boolean {
        return request.url.encodedPath.endsWith("/auth/refresh")
    }

    private fun isPublicAuthEndpoint(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.endsWith("/auth/login") || path.endsWith("/auth/register") || path.endsWith("/auth/refresh")
    }

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER = "Bearer"
        private const val TAG = "AuthInterceptor"
    }
}
