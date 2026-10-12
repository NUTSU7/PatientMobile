package com.semanticsoft.patientmobile.data.mapper.patient

import com.semanticsoft.patientmobile.core.network.dto.patient.AiSummaryResponse
import com.semanticsoft.patientmobile.core.network.dto.patient.AttentionItemDto
import com.semanticsoft.patientmobile.core.network.dto.patient.BasicIndicatorDto
import com.semanticsoft.patientmobile.core.network.dto.patient.ClinicalPillarCardDto
import com.semanticsoft.patientmobile.core.network.dto.patient.DashboardSummaryDto
import com.semanticsoft.patientmobile.core.network.dto.patient.GeneralMarkerDto
import com.semanticsoft.patientmobile.core.network.dto.patient.MarkerCategoryDto
import com.semanticsoft.patientmobile.core.network.dto.patient.MarkerSummaryDto
import com.semanticsoft.patientmobile.core.network.dto.patient.WarningCardDto
import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.domain.model.patient.AttentionItem
import com.semanticsoft.patientmobile.domain.model.patient.BasicIndicator
import com.semanticsoft.patientmobile.domain.model.patient.ClinicalPillarCard
import com.semanticsoft.patientmobile.domain.model.patient.DashboardSummary
import com.semanticsoft.patientmobile.domain.model.patient.GeneralMarker
import com.semanticsoft.patientmobile.domain.model.patient.MarkerCategory
import com.semanticsoft.patientmobile.domain.model.patient.MarkerSummary
import com.semanticsoft.patientmobile.domain.model.common.UserRole
import com.semanticsoft.patientmobile.domain.model.patient.WarningCard
import com.semanticsoft.patientmobile.domain.model.patient.WarningIndicator

fun DashboardSummaryDto.toDomain(): DashboardSummary = DashboardSummary(
    greetingName = greetingName,
    fullName = fullName,
    role = role?.let { UserRole.fromRaw(it) },
    attentionItems = attentionItems.map { it.toDomain() },
    basicIndicators = basicIndicators.map { it.toDomain() },
    markerCategories = markerCategories.map { it.toDomain() },
    generalMarkers = generalMarkers.map { it.toDomain() },
    markerSummary = markerSummary?.toDomain(),
    aiSummary = aiSummary,
    warningCards = warningCards.map { it.toDomain() },
    clinicalPillarCards = clinicalPillarCards.map { it.toDomain() }
)

fun AttentionItemDto.toDomain(): AttentionItem = AttentionItem(
    marker = marker,
    value = value,
    unit = unit,
    severity = severity
)

fun BasicIndicatorDto.toDomain(): BasicIndicator = BasicIndicator(
    title = title,
    value = value,
    unit = unit,
    status = status,
    trendDirection = trendDirection,
    trendDelta = trendDelta,
    trendDescription = trendDescription,
    referenceRange = referenceRange
)

fun MarkerCategoryDto.toDomain(): MarkerCategory = MarkerCategory(
    name = name,
    count = count
)

fun GeneralMarkerDto.toDomain(): GeneralMarker = GeneralMarker(
    title = title,
    category = category,
    value = value,
    unit = unit,
    status = status,
    normalRange = normalRange,
    borderlineRange = borderlineRange,
    attentionRange = attentionRange
)

fun MarkerSummaryDto.toDomain(): MarkerSummary = MarkerSummary(
    normal = normal,
    borderline = borderline,
    attention = attention,
    score = score
)

fun WarningCardDto.toDomain(): WarningCard = WarningCard(
    level = level,
    indicators = indicators.map { WarningIndicator(name = it.name, value = it.value, unit = it.unit) }
)

fun ClinicalPillarCardDto.toDomain(): ClinicalPillarCard = ClinicalPillarCard(
    type = type,
    reportCount = reportCount,
    alert = alert
)

fun AiSummaryResponse.toDomain(): AiSummary = AiSummary(
    summaryText = summaryText,
    status = status,
    generatedAt = generatedAt,
    latestReportId = latestReportId
)
