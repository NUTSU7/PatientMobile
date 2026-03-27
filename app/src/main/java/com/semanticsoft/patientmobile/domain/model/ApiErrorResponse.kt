package com.estcomputer.patient.domain.model

data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: String? = null
)

data class ApiErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val traceId: String?,
    val fieldErrors: List<FieldError> = emptyList()
)
