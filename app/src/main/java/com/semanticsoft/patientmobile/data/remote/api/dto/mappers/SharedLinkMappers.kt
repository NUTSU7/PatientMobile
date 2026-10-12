package com.semanticsoft.patientmobile.data.remote.api.dto.mappers

import com.semanticsoft.patientmobile.data.remote.api.dto.shared.CreateSharedLinkDto
import com.semanticsoft.patientmobile.data.remote.api.dto.shared.SharedLinkDto
import com.semanticsoft.patientmobile.domain.model.SharedLink

fun SharedLinkDto.toDomain(): SharedLink = SharedLink(
    token = token,
    documentId = documentId,
    expiresAt = expiresAt?.let(::parseInstantSafe),
    maxDownloads = maxDownloads,
    downloadCount = downloadCount,
    downloadUrl = downloadUrl
)

fun CreateSharedLinkDto.toDomain(): SharedLink = SharedLink(
    token = token,
    documentId = documentId,
    expiresAt = expiresAt?.let(::parseInstantSafe),
    maxDownloads = maxDownloads ?: 5,
    downloadUrl = downloadUrl
)
