package com.semanticsoft.patientmobile.util

import com.semanticsoft.patientmobile.domain.model.ApiErrorResponse

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
