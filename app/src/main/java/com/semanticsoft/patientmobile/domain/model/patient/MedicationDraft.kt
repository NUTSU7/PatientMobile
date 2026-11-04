package com.semanticsoft.patientmobile.domain.model.patient

import java.time.LocalDate

/**
 * Editable medication card of the "Adaugă tratament" dialog. Numeric inputs are kept as raw text,
 * exactly like the web editor, and parsed/validated in [com.semanticsoft.patientmobile.domain.usecase.patient.BuildTreatmentUseCase].
 */
data class MedicationDraft(
    val draftId: String,
    val id: String? = null,
    val name: String = "",
    val doseText: String = "",
    val doseUnit: DoseUnit? = null,
    val strength: String? = null,
    val frequencyText: String = "",
    val startDate: LocalDate,
    val durationText: String = "",
    val schedules: List<MedicationSchedule> = emptyList(),
    val mealRelation: MealRelation = MealRelation.NO_MEAL_RELATION,
    val requiresOcrReview: Boolean = false,
    val reviewed: Boolean = false,
    val unconfirmedFields: List<String> = emptyList()
)
