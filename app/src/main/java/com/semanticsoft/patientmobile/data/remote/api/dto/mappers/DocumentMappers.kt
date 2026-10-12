package com.semanticsoft.patientmobile.data.remote.api.dto.mappers

import com.semanticsoft.patientmobile.data.remote.api.dto.patient.DocumentDetailDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.DocumentDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.DuplicateMatchDto
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.PatientDocument

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
