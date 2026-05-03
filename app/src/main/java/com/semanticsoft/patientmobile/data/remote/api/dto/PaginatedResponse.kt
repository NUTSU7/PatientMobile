package com.semanticsoft.patientmobile.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class PaginatedResponse<T>(
    @SerializedName(value = "content", alternate = ["items"])
    val content: List<T> = emptyList(),
    @SerializedName(value = "pageNumber", alternate = ["page"])
    val pageNumber: Int = 0,
    @SerializedName(value = "pageSize", alternate = ["size"])
    val pageSize: Int = 20,
    @SerializedName(value = "totalElements", alternate = ["totalItems"])
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false
)
