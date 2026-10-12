package com.semanticsoft.patientmobile.domain.repository.patient

import com.semanticsoft.patientmobile.domain.model.patient.ProcessingStatus
import kotlinx.coroutines.flow.StateFlow

data class ExtractionTarget(
    val documentId: String,
    val runId: String
)

interface ProcessingCoordinator {

    val status: StateFlow<ProcessingStatus>

    fun start(targets: List<ExtractionTarget>)

    fun resumeIfPending()

    fun reset()
}
