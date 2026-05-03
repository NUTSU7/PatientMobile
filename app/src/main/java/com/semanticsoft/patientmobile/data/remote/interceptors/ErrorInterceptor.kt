package com.semanticsoft.patientmobile.data.remote.interceptors

import android.util.Log
import com.semanticsoft.patientmobile.BuildConfig
import com.semanticsoft.patientmobile.util.exceptions.ApiException
import com.semanticsoft.patientmobile.util.exceptions.DocumentScanningUnavailableException
import com.semanticsoft.patientmobile.util.exceptions.FileTooLargeException
import com.semanticsoft.patientmobile.util.exceptions.InvalidCredentialsException
import com.semanticsoft.patientmobile.util.exceptions.MalwareDetectedException
import com.semanticsoft.patientmobile.util.exceptions.RateLimitException
import com.semanticsoft.patientmobile.util.exceptions.UnsupportedMediaTypeException
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            // Log network errors but don't propagate on OkHttp thread
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Network error", e)
            }
            throw e
        }

        if (response.isSuccessful) return response

        val bodyText = response.peekBody(MAX_PEEK_BYTES).string()
        val parsed = parseErrorBody(bodyText)
        if (BuildConfig.DEBUG && !parsed.traceId.isNullOrBlank()) {
            Log.e(TAG, "HTTP ${response.code} traceId=${parsed.traceId} message=${parsed.message}")
        }

        // Return response and let repository handle errors appropriately
        return response
    }

    private fun parseErrorBody(body: String): ParsedError {
        if (body.isBlank()) return ParsedError()
        return runCatching {
            val json = JSONObject(body)
            ParsedError(
                message = json.optString("message").ifBlank { null },
                traceId = json.optString("traceId").ifBlank { null }
            )
        }.getOrElse { ParsedError() }
    }

    private data class ParsedError(
        val message: String? = null,
        val traceId: String? = null
    )

    companion object {
        private const val TAG = "ErrorInterceptor"
        private const val MAX_PEEK_BYTES = 1024 * 1024L
    }
}
