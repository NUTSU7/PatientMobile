package com.semanticsoft.patientmobile.core.util.exceptions

class RateLimitException(
    message: String = "Too many requests"
) : ApiException(message, 429)
