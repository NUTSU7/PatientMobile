package com.semanticsoft.patientmobile.core.util.exceptions

class InvalidCredentialsException(
    message: String = "Invalid credentials"
) : ApiException(message, 401)
