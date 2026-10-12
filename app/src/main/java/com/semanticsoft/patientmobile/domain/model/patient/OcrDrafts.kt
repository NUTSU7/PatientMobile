package com.semanticsoft.patientmobile.domain.model.patient

data class MedicationOcrDraft(
    val attachment: MedicalHistoryAttachment,
    val rawText: String? = null,
    val name: String? = null,
    val strength: String? = null,
    val doseValue: Double? = null,
    val doseUnit: String? = null,
    val frequencyText: String? = null,
    val durationDays: Int? = null,
    val schedules: List<MedicationSchedule> = emptyList(),
    val unconfirmedFields: List<String> = emptyList(),
    val confidence: Map<String, String> = emptyMap()
)

data class PersonalNoteOcrDraft(
    val attachment: MedicalHistoryAttachment,
    val rawText: String? = null,
    val suggestedTitle: String? = null,
    val doctorLocation: String? = null,
    val clinicalObservations: String? = null,
    val noteDate: String? = null,
    val confidence: Map<String, String> = emptyMap()
)
