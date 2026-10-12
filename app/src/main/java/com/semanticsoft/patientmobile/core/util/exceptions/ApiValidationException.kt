package com.semanticsoft.patientmobile.core.util.exceptions

import com.semanticsoft.patientmobile.core.network.dto.FieldError

class ApiValidationException(
    message: String,
    val fieldErrors: List<FieldError>
) : ApiException(message, 400)
