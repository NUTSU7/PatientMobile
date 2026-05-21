package com.semanticsoft.patientmobile.data.remote.interceptors

import android.content.Context
import android.util.Log
import com.semanticsoft.patientmobile.BuildConfig
import com.semanticsoft.patientmobile.data.local.datastore.SecureTokenStore
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(
    context: Context
) : Interceptor {
    private val tokenStore = SecureTokenStore(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authRequest = attachBearerIfNeeded(originalRequest)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Request: ${authRequest.method} ${authRequest.url}")
        }

        val response = chain.proceed(authRequest)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Response: ${response.code} for ${authRequest.url}")
        }

        return response
    }

    private fun attachBearerIfNeeded(request: Request): Request {
        if (isPublicEndpoint(request) || request.header(AUTH_HEADER) != null) {
            return request
        }

        val token = tokenStore.getAccessToken().orEmpty()
        if (token.isBlank()) return request

        return request.newBuilder()
            .header(AUTH_HEADER, "$BEARER $token")
            .build()
    }

    private fun isPublicEndpoint(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.endsWith("/auth/login")
                || path.endsWith("/auth/register")
                || path.endsWith("/auth/refresh")
    }

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER = "Bearer"
        private const val TAG = "AuthInterceptor"
    }
}
