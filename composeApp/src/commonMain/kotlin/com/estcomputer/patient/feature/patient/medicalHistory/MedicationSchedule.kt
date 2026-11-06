package com.estcomputer.patient.feature.patient.medicalHistory

data class MedicationSchedule(
    val administrationTime: String,
    val mealRelation: MealRelation,
    val mealRelationLabel: String? = null
)
