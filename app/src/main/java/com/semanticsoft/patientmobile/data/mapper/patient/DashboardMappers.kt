package com.semanticsoft.patientmobile.data.mapper.patient

import com.semanticsoft.patientmobile.core.network.dto.patient.AiSummaryResponse
import com.semanticsoft.patientmobile.domain.model.patient.AiSummary

fun AiSummaryResponse.toDomain(): AiSummary = AiSummary(
    summaryText = summaryText,
    status = status,
    generatedAt = generatedAt,
    latestReportId = latestReportId
)