package com.semanticsoft.patientmobile.util.exceptions

import com.semanticsoft.patientmobile.domain.model.FieldError

class ApiValidationException(
    message: String,
    val fieldErrors: List<FieldError>
) : ApiException(message, 400)
