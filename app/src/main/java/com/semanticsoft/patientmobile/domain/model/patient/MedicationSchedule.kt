package com.semanticsoft.patientmobile.domain.model.patient

import kotlinx.serialization.Serializable

@Serializable
data class MedicationSchedule(
    val administrationTime: String,
    val mealRelation: MealRelation,
    val mealRelationLabel: String? = null
)
