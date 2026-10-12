package com.semanticsoft.patientmobile.domain.model.patient

data class PaginatedMedicalResults(
    val items: List<MedicalResult>,
    val hasNext: Boolean
)
