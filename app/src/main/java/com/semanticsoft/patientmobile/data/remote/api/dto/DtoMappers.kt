package com.semanticsoft.patientmobile.data.remote.api.dto

import com.semanticsoft.patientmobile.domain.model.AttentionItem
import com.semanticsoft.patientmobile.domain.model.AuthResponse as DomainAuthResponse
import com.semanticsoft.patientmobile.domain.model.BasicIndicator
import com.semanticsoft.patientmobile.domain.model.ClinicalPillarCard
import com.semanticsoft.patientmobile.domain.model.DashboardSummary
import com.semanticsoft.patientmobile.domain.model.DocumentDuplicateInfo
import com.semanticsoft.patientmobile.domain.model.DoseUnit
import com.semanticsoft.patientmobile.domain.model.GeneralMarker
import com.semanticsoft.patientmobile.domain.model.MarkerCategory
import com.semanticsoft.patientmobile.domain.model.MarkerSummary
import com.semanticsoft.patientmobile.domain.model.MealRelation
import com.semanticsoft.patientmobile.domain.model.MedicalReport
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.model.Medication
import com.semanticsoft.patientmobile.domain.model.MedicationSchedule
import com.semanticsoft.patientmobile.domain.model.OcrExtraction
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.domain.model.PersonalNote
import com.semanticsoft.patientmobile.domain.model.SharedLink
import com.semanticsoft.patientmobile.domain.model.User
import com.semanticsoft.patientmobile.domain.model.WarningCard
import com.semanticsoft.patientmobile.domain.model.WarningIndicator
import java.time.Instant
import java.time.LocalDate

fun AuthResponse.toDomain(): DomainAuthResponse = DomainAuthResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType.orEmpty(),
    expiresIn = expiresIn,
    refreshExpiresIn = refreshExpiresIn,
    user = user.toDomain()
)

fun RefreshResponse.toDomain(): DomainAuthResponse = DomainAuthResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = tokenType.orEmpty(),
    expiresIn = expiresIn,
    refreshExpiresIn = refreshExpiresIn,
    user = user.toDomain()
)

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = parseDateSafe(dateOfBirth),
    role = role,
    createdAt = parseInstantSafe(createdAt)
)

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

fun OcrExtractionDto.toDomain(): OcrExtraction = OcrExtraction(
    id = id,
    documentId = documentId,
    engineName = engineName,
    status = try { OcrStatus.valueOf(status) } catch (_: Exception) { OcrStatus.PENDING },
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
    createdAt = parseInstantSafe(createdAt)
)

fun DashboardSummaryDto.toDomain(): DashboardSummary = DashboardSummary(
    greetingName = greetingName,
    fullName = fullName,
    role = role,
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

fun MedicationDto.toDomain(): Medication = Medication(
    id = id,
    name = name,
    doseValue = doseValue,
    doseUnit = try { DoseUnit.valueOf(doseUnit) } catch (_: Exception) { DoseUnit.TABLET },
    doseUnitLabel = doseUnitLabel,
    schedules = schedules.map { it.toDomain() },
    createdAt = parseInstantSafe(createdAt)
)
fun MedicationScheduleDto.toDomain(): MedicationSchedule =
    MedicationSchedule(
        administrationTime = administrationTime,
        mealRelation = try { MealRelation.valueOf(mealRelation) } catch (_: Exception) { MealRelation.NO_MEAL_RELATION },
        mealRelationLabel = mealRelationLabel
    )

fun PersonalNoteDto.toDomain(): PersonalNote = PersonalNote(
    id = id,
    analysisName = analysisName,
    doctorLocation = doctorLocation,
    clinicalObservations = clinicalObservations,
    noteDate = parseDateSafe(noteDate),
    createdAt = parseInstantSafe(createdAt)
)

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

// ── Defensive date parsers ──────────────────────────────────────────────────

@JvmSynthetic
fun parseDateSafe(raw: String): LocalDate {
    if (raw.isBlank()) return LocalDate.EPOCH
    return runCatching {
        LocalDate.parse(raw.substringBefore('T'))
    }.getOrElse {
        try {
            LocalDate.parse(raw, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {
            try {
                LocalDate.parse(raw, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            } catch (_: Exception) {
                LocalDate.EPOCH
            }
        }
    }
}

@JvmSynthetic
fun parseInstantSafe(raw: String?): Instant {
    if (raw.isNullOrBlank()) return Instant.EPOCH
    return runCatching {
        Instant.parse(raw.substringBefore(' '))
    }.getOrElse {
        try {
            Instant.parse(raw)
        } catch (_: Exception) {
            try {
                val millis = raw.toLongOrNull()
                if (millis != null) Instant.ofEpochMilli(millis) else Instant.EPOCH
            } catch (_: Exception) {
                Instant.EPOCH
            }
        }
    }
}
