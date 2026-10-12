package com.semanticsoft.patientmobile.core.util.exceptions

class UnsupportedMediaTypeException(
    message: String = "Unsupported media type"
) : ApiException(message, 415)
