package com.semanticsoft.patientmobile.util.exceptions

class FileUploadException(
    message: String = "File upload failed",
    cause: Throwable? = null
) : ApiException(message, cause = cause)
