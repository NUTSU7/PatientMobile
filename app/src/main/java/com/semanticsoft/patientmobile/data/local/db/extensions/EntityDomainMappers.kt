package com.semanticsoft.patientmobile.data.local.db.extensions

import com.semanticsoft.patientmobile.data.local.db.entity.DocumentEntity
import com.semanticsoft.patientmobile.data.local.db.entity.MedicalReportEntity
import com.semanticsoft.patientmobile.data.local.db.entity.MedicalResultEntity
import com.semanticsoft.patientmobile.data.local.db.entity.SyncStatus as EntitySyncStatus
import com.semanticsoft.patientmobile.data.local.db.entity.UserEntity
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.SyncStatus as DomainSyncStatus
import com.semanticsoft.patientmobile.domain.model.User
import java.time.Instant
import java.time.LocalDate

fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = LocalDate.parse(dateOfBirth)
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = dateOfBirth.toString()
)

fun DocumentEntity.toDomain(): PatientDocument = PatientDocument(
    id = id,
    ownerUserId = ownerUserId,
    originalFileName = originalFileName,
    mimeType = mimeType,
    fileSizeBytes = fileSizeBytes,
    uploadedAt = Instant.ofEpochMilli(uploadedAt),
    localFilePath = localFilePath,
    syncStatus = syncStatus.toDomain()
)

fun PatientDocument.toEntity(): DocumentEntity = DocumentEntity(
    id = id,
    ownerUserId = ownerUserId,
    originalFileName = originalFileName,
    mimeType = mimeType,
    fileSizeBytes = fileSizeBytes,
    uploadedAt = uploadedAt.toEpochMilli(),
    localFilePath = localFilePath,
    syncStatus = syncStatus.toEntity()
)

fun MedicalReportEntity.toDomain(): MedicalReport = MedicalReport(
    id = id,
    documentId = documentId,
    extractionRunId = extractionRunId,
    reportTitle = reportTitle,
    vendorName = vendorName,
    vendorLocation = vendorLocation,
    collectedAt = collectedAt?.let(Instant::ofEpochMilli),
    reportDate = LocalDate.parse(reportDate),
    patientName = patientName,
    patientDob = patientDob?.let(LocalDate::parse),
    patientSex = patientSex
)

fun MedicalReport.toEntity(): MedicalReportEntity = MedicalReportEntity(
    id = id,
    documentId = documentId,
    extractionRunId = extractionRunId,
    reportTitle = reportTitle,
    vendorName = vendorName,
    vendorLocation = vendorLocation,
    reportDate = reportDate.toString(),
    collectedAt = collectedAt?.toEpochMilli(),
    patientName = patientName,
    patientDob = patientDob?.toString(),
    patientSex = patientSex
)

fun UserEntity.toModel(): User = toDomain()
fun DocumentEntity.toModel(): PatientDocument = toDomain()
fun MedicalReportEntity.toModel(): MedicalReport = toDomain()
fun MedicalResultEntity.toModel(): MedicalResult = toDomain()

fun MedicalResultEntity.toDomain(): MedicalResult = MedicalResult(
    id = id,
    documentId = documentId,
    reportId = reportId,
    analysisType = analysisType,
    testName = testName,
    value = value,
    unit = unit,
    referenceRange = referenceRange,
    reportDate = LocalDate.parse(reportDate)
)

fun MedicalResult.toEntity(): MedicalResultEntity = MedicalResultEntity(
    id = id,
    documentId = documentId,
    reportId = reportId,
    analysisType = analysisType,
    testName = testName,
    value = value,
    unit = unit,
    referenceRange = referenceRange,
    reportDate = reportDate.toString()
)

private fun EntitySyncStatus.toDomain(): DomainSyncStatus = when (this) {
    EntitySyncStatus.SYNCED -> DomainSyncStatus.SYNCED
    EntitySyncStatus.PENDING -> DomainSyncStatus.PENDING
    EntitySyncStatus.FAILED -> DomainSyncStatus.FAILED
}

private fun DomainSyncStatus.toEntity(): EntitySyncStatus = when (this) {
    DomainSyncStatus.SYNCED -> EntitySyncStatus.SYNCED
    DomainSyncStatus.PENDING -> EntitySyncStatus.PENDING
    DomainSyncStatus.FAILED -> EntitySyncStatus.FAILED
}
