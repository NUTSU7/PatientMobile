package com.semanticsoft.patientmobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DoseUnit {
    TABLET,
    CAPSULE,
    ML,
    DROPS
}
