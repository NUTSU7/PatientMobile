package com.semanticsoft.patientmobile.util

fun ApiResult<*>.toUserMessage(): String = when (this) {
    is ApiResult.Success -> ""
    is ApiResult.HttpError -> message.ifBlank { "Server error (HTTP $code)." }
    is ApiResult.NetworkError -> "No internet connection. Check your network and try again."
    is ApiResult.AuthError -> "Session expired. Please log in again."
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

@Suppress("UNCHECKED_CAST")
inline fun <T> ApiResult<T>.onError(action: (ApiResult<Nothing>) -> Unit): ApiResult<T> {
    if (this !is ApiResult.Success) {
        val error = this as ApiResult<Nothing>
        action(error)
    }
    return this
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> try {
        ApiResult.Success(transform(data))
    } catch (e: Exception) {
        ApiResult.HttpError(code = -1, message = e.message ?: "Mapping error")
    }
    is ApiResult.HttpError -> this
    is ApiResult.NetworkError -> this
    is ApiResult.AuthError -> this
}
