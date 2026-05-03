package com.semanticsoft.patientmobile.util.exceptions

class DocumentScanningUnavailableException(
    message: String = "Document scanning service unavailable"
) : ApiException(message, 503)
