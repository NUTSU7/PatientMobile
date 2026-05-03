package com.semanticsoft.patientmobile.util

import com.semanticsoft.patientmobile.util.exceptions.DocumentScanningUnavailableException
import com.semanticsoft.patientmobile.util.exceptions.FileTooLargeException
import com.semanticsoft.patientmobile.util.exceptions.InvalidCredentialsException
import com.semanticsoft.patientmobile.util.exceptions.MalwareDetectedException
import com.semanticsoft.patientmobile.util.exceptions.RateLimitException
import com.semanticsoft.patientmobile.util.exceptions.UnsupportedMediaTypeException

fun Throwable.toUserMessage(): String = when (this) {
    is InvalidCredentialsException -> "Session expired. Please log in again."
    is RateLimitException -> "Too many attempts, please wait a few minutes."
    is FileTooLargeException -> "File too large (max 10 MB)."
    is UnsupportedMediaTypeException -> "Unsupported format (pdf, jpg, jpeg, png only)."
    is MalwareDetectedException -> "File failed security scan."
    is DocumentScanningUnavailableException -> "Server unavailable, try again later."
    else -> message ?: "Something went wrong. Please try again."
}
