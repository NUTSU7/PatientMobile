package com.semanticsoft.patientmobile.domain.model.patient

data class DocumentDuplicateInfo(
    val checksum: String,
    val existingDocumentId: String,
    val existingFileName: String,
    val existingUploadedAt: String
)
