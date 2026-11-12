package com.estcomputer.patient.domain.model

public enum class WipeStepResult {
    DONE,

    /** The step did not finish. Running it again is always safe. */
    FAILED,
}

public enum class WipeResult {
    DONE,

    /** Something is left behind. The marker stays, so the next start-up runs the wipe again. */
    INCOMPLETE,
}
