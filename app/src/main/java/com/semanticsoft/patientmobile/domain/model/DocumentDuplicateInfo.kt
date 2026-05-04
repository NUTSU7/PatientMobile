package com.semanticsoft.patientmobile.domain.model

data class DocumentDuplicateInfo(
    val checksum: String,
    val existingFileName: String
)
