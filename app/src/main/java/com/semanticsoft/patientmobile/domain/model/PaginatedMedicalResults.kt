package com.semanticsoft.patientmobile.domain.model

data class PaginatedMedicalResults(
    val items: List<MedicalResult>,
    val hasNext: Boolean
)
