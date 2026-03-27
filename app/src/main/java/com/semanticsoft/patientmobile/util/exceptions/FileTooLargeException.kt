package com.semanticsoft.patientmobile.util.exceptions

class FileTooLargeException(
    message: String = "File too large"
) : ApiException(message, 413)
