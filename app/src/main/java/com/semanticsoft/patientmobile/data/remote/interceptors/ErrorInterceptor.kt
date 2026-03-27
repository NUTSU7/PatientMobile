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
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        val bodyText = response.peekBody(MAX_PEEK_BYTES).string()
        val parsed = parseErrorBody(bodyText)
        if (BuildConfig.DEBUG && !parsed.traceId.isNullOrBlank()) {
            Log.e(TAG, "HTTP ${response.code} traceId=${parsed.traceId} message=${parsed.message}")
        }

        val message = parsed.message ?: "HTTP ${response.code}"

        throw when (response.code) {
            401 -> InvalidCredentialsException(message)
            413 -> FileTooLargeException(message)
            415 -> UnsupportedMediaTypeException(message)
            422 -> MalwareDetectedException(message)
            429 -> RateLimitException(message)
            503 -> DocumentScanningUnavailableException(message)
            else -> ApiException(message, response.code)
        }
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
