package com.semanticsoft.patientmobile.util.exceptions

open class ApiException(
    message: String,
    val code: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)
