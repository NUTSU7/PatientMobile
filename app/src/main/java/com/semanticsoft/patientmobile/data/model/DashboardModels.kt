package com.semanticsoft.patientmobile.data.model

data class AttentionItem(
    val marker: String,
    val value: String,
    val unit: String,
    val severity: String
)

data class MarkerSummary(
    val normal: Int,
    val borderline: Int,
    val attention: Int,
    val score: Int
)
