package com.semanticsoft.patientmobile.util

@Deprecated(
    message = "Replaced by ApiResult",
    replaceWith = ReplaceWith("ApiResult", "com.semanticsoft.patientmobile.util.ApiResult")
)
sealed class Resource<out T> {
    @Deprecated("Use ApiResult.Success")
    data class Success<T>(val data: T) : Resource<T>()
    @Deprecated("Use ApiResult.Loading concept in UiState")
    data object Loading : Resource<Nothing>()
    @Deprecated("Use ApiResult.HttpError or ApiResult.NetworkError")
    data class Error(val message: String) : Resource<Nothing>()
}
