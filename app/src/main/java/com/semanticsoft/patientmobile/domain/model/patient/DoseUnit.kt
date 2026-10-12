package com.semanticsoft.patientmobile.domain.model.patient

import kotlinx.serialization.Serializable

@Serializable
enum class DoseUnit {
    TABLET,
    CAPSULE,
    ML,
    DROPS
}
