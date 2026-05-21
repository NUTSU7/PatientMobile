package com.semanticsoft.patientmobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MealRelation {
    BEFORE_MEAL,
    WITH_MEAL,
    AFTER_MEAL,
    NO_MEAL_RELATION
}
