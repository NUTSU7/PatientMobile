package com.semanticsoft.patientmobile.data.remote.api.dto.mappers

import com.semanticsoft.patientmobile.data.remote.api.dto.patient.AttentionItemDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.BasicIndicatorDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.ClinicalPillarCardDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.DashboardSummaryDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.GeneralMarkerDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.MarkerCategoryDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.MarkerSummaryDto
import com.semanticsoft.patientmobile.data.remote.api.dto.patient.WarningCardDto
import com.semanticsoft.patientmobile.domain.model.AttentionItem
import com.semanticsoft.patientmobile.domain.model.BasicIndicator
import com.semanticsoft.patientmobile.domain.model.ClinicalPillarCard
import com.semanticsoft.patientmobile.domain.model.DashboardSummary
import com.semanticsoft.patientmobile.domain.model.GeneralMarker
import com.semanticsoft.patientmobile.domain.model.MarkerCategory
import com.semanticsoft.patientmobile.domain.model.MarkerSummary
import com.semanticsoft.patientmobile.domain.model.UserRole
import com.semanticsoft.patientmobile.domain.model.WarningCard
import com.semanticsoft.patientmobile.domain.model.WarningIndicator

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
