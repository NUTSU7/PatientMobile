package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DuplicateCheckRequest(
    val checksums: List<String>
)

@Serializable
data class DuplicateCheckResponse(
    val matches: List<DuplicateMatchDto> = emptyList()
)

@Serializable
data class DuplicateMatchDto(
    val checksum: String,
    @SerialName("existingDocumentId") val existingDocumentId: String,
    @SerialName("existingUploadedAt") val existingUploadedAt: String,
    @SerialName("existingFileName") val existingFileName: String
)

@Serializable
data class DocumentDto(
    val id: String,
    @SerialName("originalFileName") val originalFileName: String,
    @SerialName("fileType") val fileType: String,
    @SerialName("fileSizeBytes") val fileSizeBytes: Long,
    @SerialName("uploadedAt") val uploadedAt: String,
    @SerialName("observedAt") val observedAt: String? = null,
    val categories: List<String> = emptyList()
)

@Serializable
data class DocumentDetailDto(
    val id: String,
    @SerialName("originalFileName") val originalFileName: String,
    @SerialName("fileType") val fileType: String,
    @SerialName("fileSizeBytes") val fileSizeBytes: Long,
    @SerialName("uploadedAt") val uploadedAt: String,
    @SerialName("observedAt") val observedAt: String? = null,
    val categories: List<String> = emptyList(),
    @SerialName("sha256Checksum") val sha256Checksum: String? = null
)

@Serializable
data class RenameDocumentRequest(
    @SerialName("originalFileName") val originalFileName: String
)

@Serializable
data class CreateShareLinkRequest(
    @SerialName("maxDownloads") val maxDownloads: Int? = null,
    @SerialName("expiresInHours") val expiresInHours: Int? = null
)
