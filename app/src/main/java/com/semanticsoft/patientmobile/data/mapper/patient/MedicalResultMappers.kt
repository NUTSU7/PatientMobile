package com.semanticsoft.patientmobile.data.mapper.patient

import com.semanticsoft.patientmobile.core.network.dto.patient.MedicalResultDto
import com.semanticsoft.patientmobile.core.network.dto.patient.normalizeTestName
import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.data.mapper.common.parseDateSafe

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
