package com.semanticsoft.patientmobile.data.remote.api.dto.mappers

import com.semanticsoft.patientmobile.data.remote.api.dto.patient.MedicalResultDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.normalizeTestName
import com.semanticsoft.patientmobile.domain.model.MedicalResult

fun MedicalResultDto.toDomain(): MedicalResult = MedicalResult(
    id = id ?: testDefinitionId ?: "",
    testDefinitionId = testDefinitionId,
    documentId = documentId,
    reportId = reportId,
    originalTestName = originalTestName,
    canonicalName = canonicalName.normalizeTestName(),
    analysisGroup = analysisGroup,
    valueNumeric = valueNumeric,
    valueText = valueText,
    unit = unit,
    referenceLow = referenceLow,
    referenceHigh = referenceHigh,
    referenceText = referenceText,
    abnormalFlag = abnormalFlag,
    observedAt = observedAt?.let(::parseDateSafe)
)
