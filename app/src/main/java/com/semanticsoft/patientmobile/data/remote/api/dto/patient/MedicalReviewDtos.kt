package com.semanticsoft.patientmobile.data.remote.api.dto.patient

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMedicalReportReviewRequest(
    val observedAt: String? = null,
    val markReviewed: Boolean? = null,
    val displayTitle: String? = null,
    val displaySubtitle: String? = null,
    val sourceLaboratory: String? = null
)
