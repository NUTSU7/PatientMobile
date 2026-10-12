package com.semanticsoft.patientmobile.core.util.exceptions

class DocumentScanningUnavailableException(
    message: String = "Document scanning service unavailable"
) : ApiException(message, 503)
