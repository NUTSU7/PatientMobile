package com.semanticsoft.patientmobile.core.util

import com.semanticsoft.patientmobile.core.network.dto.ApiErrorResponse

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class HttpError(
        val code: Int,
        val message: String,
        val errorBody: ApiErrorResponse? = null
    ) : ApiResult<Nothing>()
    data object NetworkError : ApiResult<Nothing>()
    data object AuthError : ApiResult<Nothing>()
}
