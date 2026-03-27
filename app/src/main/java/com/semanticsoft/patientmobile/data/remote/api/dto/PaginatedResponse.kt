package com.semanticsoft.patientmobile.data.remote.api.dto

data class PaginatedResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int
)
