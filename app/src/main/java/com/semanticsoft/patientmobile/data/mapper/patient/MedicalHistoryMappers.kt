package com.semanticsoft.patientmobile.data.mapper.patient

import com.semanticsoft.patientmobile.core.network.dto.patient.MedicalHistoryAttachmentDto
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicationDto
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicationOcrDraftResponseDto
import com.semanticsoft.patientmobile.core.network.dto.patient.MedicationScheduleDto
import com.semanticsoft.patientmobile.core.network.dto.patient.PersonalNoteDto
import com.semanticsoft.patientmobile.core.network.dto.patient.PersonalNoteOcrDraftResponseDto
import com.semanticsoft.patientmobile.domain.model.patient.DoseUnit
import com.semanticsoft.patientmobile.domain.model.patient.MealRelation
import com.semanticsoft.patientmobile.domain.model.patient.MedicalHistoryAttachment
import com.semanticsoft.patientmobile.domain.model.patient.Medication
import com.semanticsoft.patientmobile.domain.model.patient.MedicationOcrDraft
import com.semanticsoft.patientmobile.domain.model.patient.MedicationSchedule
import com.semanticsoft.patientmobile.domain.model.patient.PersonalNote
import com.semanticsoft.patientmobile.domain.model.patient.PersonalNoteOcrDraft
import com.semanticsoft.patientmobile.data.mapper.common.parseDateSafe
import com.semanticsoft.patientmobile.data.mapper.common.parseInstantSafe

fun MedicationDto.toDomain(): Medication = Medication(
    id = id,
    name = name,
    doseValue = doseValue,
    doseUnit = try { DoseUnit.valueOf(doseUnit) } catch (_: Exception) { DoseUnit.TABLET },
    doseUnitLabel = doseUnitLabel,
    schedules = schedules.map { it.toDomain() },
    active = active,
    effectiveDate = effectiveDate?.let(::parseDateSafe),
    endDate = endDate?.let(::parseDateSafe),
    analysisDocumentId = analysisDocumentId,
    attachmentIds = attachments.map { it.id },
    ocrReviewConfirmed = ocrReviewConfirmed,
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
    analysisDocumentId = analysisDocumentId,
    attachmentIds = attachments.map { it.id },
    createdAt = parseInstantSafe(createdAt)
)

fun MedicalHistoryAttachmentDto.toDomain(): MedicalHistoryAttachment = MedicalHistoryAttachment(
    id = id,
    originalFileName = originalFileName,
    fileType = fileType,
    fileSizeBytes = fileSizeBytes
)

fun MedicationOcrDraftResponseDto.toDomain(): MedicationOcrDraft = MedicationOcrDraft(
    attachment = attachment.toDomain(),
    rawText = rawText,
    name = name,
    strength = strength,
    doseValue = doseValue,
    doseUnit = doseUnit,
    frequencyText = frequencyText,
    durationDays = durationDays,
    schedules = schedules.map { it.toDomain() },
    unconfirmedFields = unconfirmedFields,
    confidence = confidence
)

fun PersonalNoteOcrDraftResponseDto.toDomain(): PersonalNoteOcrDraft = PersonalNoteOcrDraft(
    attachment = attachment.toDomain(),
    rawText = rawText,
    suggestedTitle = suggestedTitle,
    doctorLocation = doctorLocation,
    clinicalObservations = clinicalObservations,
    noteDate = noteDate,
    confidence = confidence
)
