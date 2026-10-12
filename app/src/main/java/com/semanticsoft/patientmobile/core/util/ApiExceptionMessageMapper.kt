package com.semanticsoft.patientmobile.core.util

import com.semanticsoft.patientmobile.core.util.exceptions.DocumentScanningUnavailableException
import com.semanticsoft.patientmobile.core.util.exceptions.FileTooLargeException
import com.semanticsoft.patientmobile.core.util.exceptions.InvalidCredentialsException
import com.semanticsoft.patientmobile.core.util.exceptions.MalwareDetectedException
import com.semanticsoft.patientmobile.core.util.exceptions.RateLimitException
import com.semanticsoft.patientmobile.core.util.exceptions.UnsupportedMediaTypeException

fun Throwable.toUserMessage(): String = when (this) {
    is InvalidCredentialsException -> "Session expired. Please log in again."
    is RateLimitException -> "Too many attempts, please wait a few minutes."
    is FileTooLargeException -> "File too large (max 10 MB)."
    is UnsupportedMediaTypeException -> "Unsupported format (pdf, jpg, jpeg, png only)."
    is MalwareDetectedException -> "File failed security scan."
    is DocumentScanningUnavailableException -> "Server unavailable, try again later."
    else -> message ?: "Something went wrong. Please try again."
}
