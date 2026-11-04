package com.semanticsoft.patientmobile.domain.usecase.patient

import com.semanticsoft.patientmobile.domain.model.patient.Medication
import com.semanticsoft.patientmobile.domain.model.patient.MedicationDraft
import com.semanticsoft.patientmobile.domain.model.patient.Treatment
import javax.inject.Inject

sealed interface BuildTreatmentResult {
    data class Valid(val treatment: Treatment) : BuildTreatmentResult
    data class Invalid(val message: String) : BuildTreatmentResult
}

/**
 * Validates the "Adaugă tratament" drafts with the same rules as the web `saveTreatmentDraft`
 * and maps them to a [Treatment].
 */
class BuildTreatmentUseCase @Inject constructor() {

    operator fun invoke(
        treatmentName: String,
        drafts: List<MedicationDraft>,
        analysisDocumentIds: List<String>,
        attachmentIds: List<String>
    ): BuildTreatmentResult {
        val name = treatmentName.trim()
        if (name.isEmpty() || drafts.isEmpty()) {
            return BuildTreatmentResult.Invalid("Completează denumirea tratamentului și cel puțin un medicament.")
        }
        val medications = drafts.map { draft ->
            when (val result = draft.toMedication(analysisDocumentIds)) {
                is DraftResult.Ok -> result.medication
                is DraftResult.Error -> return BuildTreatmentResult.Invalid(result.message)
            }
        }
        return BuildTreatmentResult.Valid(Treatment(name, medications, attachmentIds))
    }

    private sealed interface DraftResult {
        data class Ok(val medication: Medication) : DraftResult
        data class Error(val message: String) : DraftResult
    }

    private fun MedicationDraft.toMedication(analysisDocumentIds: List<String>): DraftResult {
        if (name.isBlank() || (requiresOcrReview && !reviewed)) {
            return DraftResult.Error("Verifică și confirmă fiecare medicament înainte de salvare.")
        }
        val dose = doseText.trim().replace(',', '.').takeIf { it.isNotEmpty() }?.let { text ->
            text.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
                ?: return DraftResult.Error("Doza trebuie să fie pozitivă sau necompletată.")
        }
        val duration = durationText.trim().takeIf { it.isNotEmpty() }?.let { text ->
            text.toIntOrNull()?.takeIf { it in MIN_DURATION_DAYS..MAX_DURATION_DAYS }
                ?: return DraftResult.Error("Durata trebuie să fie între 1 și 3650 zile.")
        }
        val seenTimes = mutableSetOf<String>()
        schedules.forEach { schedule ->
            if (!TIME_REGEX.matches(schedule.administrationTime) || !seenTimes.add(schedule.administrationTime)) {
                return DraftResult.Error("Introdu ore distincte în format HH:mm.")
            }
        }
        return DraftResult.Ok(
            Medication(
                id = id.orEmpty(),
                name = name.trim(),
                doseValue = dose,
                doseUnit = doseUnit,
                strength = strength,
                frequencyText = frequencyText.trim().ifEmpty { null },
                schedules = schedules,
                effectiveDate = startDate,
                endDate = duration?.let { startDate.plusDays((it - 1).toLong()) },
                analysisDocumentId = analysisDocumentIds.firstOrNull(),
                analysisDocumentIds = analysisDocumentIds,
                ocrReviewConfirmed = true
            )
        )
    }

    private companion object {
        const val MIN_DURATION_DAYS = 1
        const val MAX_DURATION_DAYS = 3650
        val TIME_REGEX = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
    }
}
