package com.semanticsoft.patientmobile.util.exceptions

class RateLimitException(
    message: String = "Too many requests"
) : ApiException(message, 429)
