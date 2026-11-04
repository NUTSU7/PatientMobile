package com.semanticsoft.patientmobile.domain.model.patient

data class MedicalHistoryLinkedDocument(
    val id: String,
    val originalFileName: String? = null,
    val displayTitle: String? = null,
    val displaySubtitle: String? = null,
    val sourceLaboratory: String? = null
)
