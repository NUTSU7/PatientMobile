package com.semanticsoft.patientmobile.data.remote

import com.semanticsoft.patientmobile.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object SafeApiCall {

    suspend fun <T> safeApiCall(
        call: suspend () -> T
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            ApiResult.Success(call())
        } catch (e: HttpException) {
            if (e.code() == 401) {
                ApiResult.AuthError
            } else {
                ApiResult.HttpError(
                    code = e.code(),
                    message = e.message().orEmpty()
                )
            }
        } catch (e: UnknownHostException) {
            ApiResult.NetworkError
        } catch (e: SocketTimeoutException) {
            ApiResult.NetworkError
        } catch (e: IOException) {
            if (e.message?.contains("Unable to resolve host") == true
                || e.message?.contains("timeout") == true
            ) {
                ApiResult.NetworkError
            } else {
                ApiResult.HttpError(code = -1, message = e.message ?: "Network error")
            }
        } catch (e: Exception) {
            ApiResult.HttpError(code = -1, message = e.message ?: "Unexpected error")
        }
    }
}
