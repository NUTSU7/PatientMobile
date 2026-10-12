package com.semanticsoft.patientmobile.core.network.dto.patient

import kotlinx.serialization.Serializable

@Serializable
data class BulkDeleteRequest(
    val documentIds: List<String>
)
