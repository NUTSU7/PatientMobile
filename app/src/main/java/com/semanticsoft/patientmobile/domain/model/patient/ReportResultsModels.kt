package com.semanticsoft.patientmobile.domain.model.patient

data class ResultItem(
    val id: String,
    val testName: String,
    val value: String,
    val unit: String,
    val reference: String,
    val abnormalFlag: String?
)

data class ResultGroup(
    val groupName: String,
    val results: List<ResultItem>
)
