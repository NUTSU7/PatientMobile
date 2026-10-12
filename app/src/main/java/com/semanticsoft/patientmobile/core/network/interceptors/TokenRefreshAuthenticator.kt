package com.semanticsoft.patientmobile.core.network.interceptors

import android.util.Log
import com.semanticsoft.patientmobile.BuildConfig
import com.semanticsoft.patientmobile.core.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(
    private val tokenManager: TokenManager,
    private val refreshClient: OkHttpClient,
    private val json: Json
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Authenticator called for: ${response.request.url}")
        }

        if (response.request.header(HEADER_RETRY) != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Already retried — propagating 401")
            }
            tokenManager.clearTokens()
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentAccessToken = tokenManager.getAccessToken()
                val originalToken = response.request.header(AUTH_HEADER)?.removePrefix("$BEARER ")

                if (!currentAccessToken.isNullOrBlank() && currentAccessToken != originalToken) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Token already refreshed by another thread — retrying with new token")
                    }
                    response.request.newBuilder()
                        .header(AUTH_HEADER, "$BEARER $currentAccessToken")
                        .header(HEADER_RETRY, "1")
                        .build()
                } else {
                    val refreshToken = tokenManager.getRefreshToken()
                    if (refreshToken.isNullOrBlank()) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "No refresh token — propagating 401")
                        }
                        tokenManager.clearTokens()
                        null
                    } else {
                        val newTokens = performRefresh(refreshToken)
                        if (newTokens != null) {
                            val (accessToken, newRefreshToken) = newTokens
                            tokenManager.saveTokens(accessToken, newRefreshToken)
                            response.request.newBuilder()
                                .header(AUTH_HEADER, "$BEARER $accessToken")
                                .header(HEADER_RETRY, "1")
                                .build()
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "Refresh failed — clearing tokens, propagating 401")
                            }
                            tokenManager.clearTokens()
                            null
                        }
                    }
                }
            }
        }
    }

    private fun performRefresh(refreshToken: String): Pair<String, String>? {
        return try {
            val baseUrl = com.semanticsoft.patientmobile.BuildConfig.BASE_URL
            val refreshUrl = "${baseUrl}auth/refresh"

            val payload = "{\"refreshToken\":\"$refreshToken\"}"
            val requestBody = payload.toRequestBody("application/json; charset=utf-8".toMediaType())

            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .build()

            val refreshResponse = refreshClient.newCall(refreshRequest).execute()
            refreshResponse.use { res ->
                if (!res.isSuccessful) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Refresh response: ${res.code}")
                    }
                    return null
                }

                val bodyText = res.body?.string().orEmpty()
                if (bodyText.isBlank()) return null

                val parsed = try {
                    json.parseToJsonElement(bodyText).jsonObject
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Failed to parse refresh response: ${e.message}")
                    }
                    null
                }

                val accessToken = parsed?.get("accessToken")?.jsonPrimitive?.content
                val newRefreshToken = parsed?.get("refreshToken")?.jsonPrimitive?.content

                if (accessToken.isNullOrBlank() || newRefreshToken.isNullOrBlank()) {
                    return null
                }

                Pair(accessToken, newRefreshToken)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Refresh network error: ${e.message}")
            }
            null
        }
    }

    companion object {
        private const val TAG = "TokenRefreshAuthenticator"
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER = "Bearer"
        private const val HEADER_RETRY = "X-Auth-Retry"
    }
}
