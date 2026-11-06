package com.estcomputer.patient.feature.patient.medicalHistory

import java.time.LocalDate

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
