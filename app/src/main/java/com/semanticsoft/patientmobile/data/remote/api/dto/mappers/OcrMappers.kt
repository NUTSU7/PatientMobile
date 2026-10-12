package com.semanticsoft.patientmobile.data.remote.api.dto.mappers

import com.semanticsoft.patientmobile.data.remote.api.dto.patient.OcrExtractionDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.OcrReportDto
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.OcrExtraction
import com.semanticsoft.patientmobile.domain.model.OcrStatus

fun OcrExtractionDto.toDomain(): OcrExtraction = OcrExtraction(
    id = id,
    documentId = documentId,
    engineName = engineName,
    status = mapOcrStatus(status),
    rawText = rawText,
    reports = reports.map { it.toDomain() },
    createdAt = parseInstantSafe(createdAt)
)

fun OcrReportDto.toDomain(): MedicalReport = MedicalReport(
    id = id,
    documentId = "", // report DTO doesn't carry documentId directly — set by caller
    engineName = engineName,
    title = title.orEmpty(),
    status = try { status?.let(OcrStatus::valueOf) } catch (_: Exception) { null },
    summary = summary,
    clinicalType = clinicalType,
    clinicalSubtype = clinicalSubtype,
    requiresReview = requiresReview,
    resultCount = resultCount,
    createdAt = parseInstantSafe(createdAt)
)
