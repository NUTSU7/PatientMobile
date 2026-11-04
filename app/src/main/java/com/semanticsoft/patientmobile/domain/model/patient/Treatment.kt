package com.semanticsoft.patientmobile.domain.model.patient

/** A named group of medications saved together (web "tratament"). */
data class Treatment(
    val name: String,
    val medications: List<Medication>,
    val attachmentIds: List<String> = emptyList()
)
