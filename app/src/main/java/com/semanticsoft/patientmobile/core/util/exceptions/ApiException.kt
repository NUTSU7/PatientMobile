package com.semanticsoft.patientmobile.core.util.exceptions

open class ApiException(
    message: String,
    val code: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)
