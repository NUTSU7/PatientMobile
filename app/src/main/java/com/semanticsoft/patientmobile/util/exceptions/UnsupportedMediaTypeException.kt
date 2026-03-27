package com.semanticsoft.patientmobile.util.exceptions

class UnsupportedMediaTypeException(
    message: String = "Unsupported media type"
) : ApiException(message, 415)
