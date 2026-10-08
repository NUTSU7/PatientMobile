package com.semanticsoft.patientmobile.domain.model

enum class ProcessingOutcome {
    COMPLETED,
    NO_RESULTS,
    NON_LAB
}

data class ProcessingStatus(
    val isProcessing: Boolean = false,
    val totalJobs: Int = 0,
    val completedJobs: Int = 0,
    val failedJobs: Int = 0,
    val nonLabReports: Int = 0,
    val isSlowWarning: Boolean = false,
    val isTimedOut: Boolean = false,
    val message: String = "",
    val lastOutcome: ProcessingOutcome? = null,
    val aiRegenerationInFlight: Boolean = false
) {
    val pendingJobs: Int get() = (totalJobs - completedJobs - failedJobs).coerceAtLeast(0)
    val isDone: Boolean get() = totalJobs > 0 && pendingJobs == 0
}
