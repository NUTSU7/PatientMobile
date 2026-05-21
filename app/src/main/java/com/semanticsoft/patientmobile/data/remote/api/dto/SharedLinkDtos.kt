package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SharedLinkDto(
    val token: String,
    @SerialName("documentId") val documentId: String,
    @SerialName("expiresAt") val expiresAt: String? = null,
    @SerialName("maxDownloads") val maxDownloads: Int = 5,
    @SerialName("downloadCount") val downloadCount: Int = 0,
    @SerialName("downloadUrl") val downloadUrl: String? = null
)

@Serializable
data class CreateSharedLinkDto(
    val token: String,
    @SerialName("documentId") val documentId: String,
    @SerialName("expiresAt") val expiresAt: String? = null,
    @SerialName("maxDownloads") val maxDownloads: Int? = null,
    @SerialName("downloadUrl") val downloadUrl: String? = null
)
