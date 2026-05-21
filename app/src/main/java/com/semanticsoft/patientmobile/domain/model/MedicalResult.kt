package com.semanticsoft.patientmobile.domain.model

import java.time.LocalDate

data class MedicalResult(
    val id: String,
    val testDefinitionId: String? = null,
    val documentId: String,
    val reportId: String? = null,
    val originalTestName: String,
    val canonicalName: String,
    val analysisGroup: String,
    val valueNumeric: Double? = null,
    val valueText: String? = null,
    val unit: String,
    val referenceLow: Double? = null,
    val referenceHigh: Double? = null,
    val referenceText: String? = null,
    val abnormalFlag: String? = null,
    val observedAt: LocalDate? = null
)
