package com.semanticsoft.patientmobile.domain.model.patient

data class MedicalHistoryAttachment(
    val id: String,
    val originalFileName: String,
    val fileType: String,
    val fileSizeBytes: Long
)
