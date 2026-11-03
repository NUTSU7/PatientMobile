package com.semanticsoft.patientmobile.data.mapper.patient

import com.semanticsoft.patientmobile.core.network.dto.patient.CreateSharedLinkDto
import com.semanticsoft.patientmobile.core.network.dto.patient.SharedLinkDto
import com.semanticsoft.patientmobile.domain.model.patient.SharedLink
import com.semanticsoft.patientmobile.data.mapper.common.parseInstantSafe

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
