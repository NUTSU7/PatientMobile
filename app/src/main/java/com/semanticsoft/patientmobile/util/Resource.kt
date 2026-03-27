package com.semanticsoft.patientmobile.util

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data object Loading : Resource<Nothing>()
    data class Error(val message: String) : Resource<Nothing>()
}
