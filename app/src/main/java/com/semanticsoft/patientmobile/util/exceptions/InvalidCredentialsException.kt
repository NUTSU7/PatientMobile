package com.semanticsoft.patientmobile.util.exceptions

class InvalidCredentialsException(
    message: String = "Invalid credentials"
) : ApiException(message, 401)
