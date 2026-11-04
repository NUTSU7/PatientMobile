package com.semanticsoft.patientmobile.domain.model.patient

data class MedicationOcrItem(
    val name: String? = null,
    val strength: String? = null,
    val doseValue: Double? = null,
    val doseUnit: String? = null,
    val frequencyText: String? = null,
    val durationDays: Int? = null,
    val schedules: List<MedicationSchedule> = emptyList(),
    val unconfirmedFields: List<String> = emptyList()
)

data class MedicationOcrDraft(
    val attachment: MedicalHistoryAttachment,
    val rawText: String? = null,
    val suggestedTreatmentName: String? = null,
    val medications: List<MedicationOcrItem> = emptyList(),
    val name: String? = null,
    val strength: String? = null,
    val doseValue: Double? = null,
    val doseUnit: String? = null,
    val frequencyText: String? = null,
    val durationDays: Int? = null,
    val schedules: List<MedicationSchedule> = emptyList(),
    val unconfirmedFields: List<String> = emptyList(),
    val confidence: Map<String, String> = emptyMap()
) {
    /** Web behaviour: use `medications[]` when present, otherwise the draft itself as the only item. */
    val items: List<MedicationOcrItem>
        get() = medications.ifEmpty {
            listOf(
                MedicationOcrItem(
                    name = name,
                    strength = strength,
                    doseValue = doseValue,
                    doseUnit = doseUnit,
                    frequencyText = frequencyText,
                    durationDays = durationDays,
                    schedules = schedules,
                    unconfirmedFields = unconfirmedFields
                )
            )
        }
}

data class PersonalNoteOcrDraft(
    val attachment: MedicalHistoryAttachment,
    val rawText: String? = null,
    val suggestedTitle: String? = null,
    val doctorLocation: String? = null,
    val clinicalObservations: String? = null,
    val noteDate: String? = null,
    val confidence: Map<String, String> = emptyMap()
)
