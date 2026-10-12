package com.semanticsoft.patientmobile.data.remote.api.dto.patient

import kotlinx.serialization.Serializable

@Serializable
data class BulkDeleteRequest(
    val documentIds: List<String>
)
