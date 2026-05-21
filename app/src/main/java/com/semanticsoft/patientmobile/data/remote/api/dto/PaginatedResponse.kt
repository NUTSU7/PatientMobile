package com.semanticsoft.patientmobile.data.remote.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    @SerialName("items") val content: List<T> = emptyList(),
    @SerialName("page") val page: Int = 0,
    @SerialName("size") val size: Int = 20,
    @SerialName("totalItems") val totalElements: Long = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
    @SerialName("hasNext") val hasNext: Boolean = false
)
