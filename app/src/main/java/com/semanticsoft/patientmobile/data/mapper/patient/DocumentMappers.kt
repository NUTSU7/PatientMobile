package com.semanticsoft.patientmobile.data.mapper.patient

import com.semanticsoft.patientmobile.core.network.dto.patient.DocumentDetailDto
import com.semanticsoft.patientmobile.core.network.dto.patient.DocumentDto
import com.semanticsoft.patientmobile.core.network.dto.patient.DuplicateMatchDto
import com.semanticsoft.patientmobile.domain.model.patient.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.patient.PatientDocument
import com.semanticsoft.patientmobile.data.mapper.common.parseInstantSafe

fun DocumentDto.toDomain(): PatientDocument = PatientDocument(
    id = id,
    originalFileName = originalFileName,
    mimeType = fileType,
    fileSizeBytes = fileSizeBytes,
    uploadedAt = parseInstantSafe(uploadedAt),
    observedAt = observedAt?.let(::parseInstantSafe),
    categories = categories
)

fun DocumentDetailDto.toDomain(): PatientDocument = PatientDocument(
    id = id,
    originalFileName = originalFileName,
    mimeType = fileType,
    fileSizeBytes = fileSizeBytes,
    uploadedAt = parseInstantSafe(uploadedAt),
    observedAt = observedAt?.let(::parseInstantSafe),
    categories = categories,
    sha256Checksum = sha256Checksum
)

fun DuplicateMatchDto.toDomain(): DocumentDuplicateInfo = DocumentDuplicateInfo(
    checksum = checksum,
    existingDocumentId = existingDocumentId,
    existingFileName = existingFileName,
    existingUploadedAt = existingUploadedAt
)
