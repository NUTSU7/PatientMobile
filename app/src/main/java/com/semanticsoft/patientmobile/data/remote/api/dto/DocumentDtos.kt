package com.semanticsoft.patientmobile.data.remote.api.dto

data class DuplicateCheckRequest(
    val checksums: List<String>
)

data class DuplicateCheckResponse(
    val matches: List<DuplicateMatchDto> = emptyList()
)

data class DuplicateMatchDto(
    val checksum: String,
    val existingDocumentId: String,
    val existingUploadedAt: String,
    val existingFileName: String
)
