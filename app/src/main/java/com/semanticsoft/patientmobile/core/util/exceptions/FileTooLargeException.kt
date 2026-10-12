package com.semanticsoft.patientmobile.core.util.exceptions

class FileTooLargeException(
    message: String = "File too large"
) : ApiException(message, 413)
