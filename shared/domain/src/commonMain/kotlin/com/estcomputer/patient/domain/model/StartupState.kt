package com.estcomputer.patient.domain.model

public enum class StartupState {
    /** No vault and no data files: first-time setup is allowed. */
    Clean,

    VaultPresent,

    /** A previous wipe did not finish; it must be completed before any UI. */
    WipeInterrupted,

    /** Data files exist without a vault. Never set up over them; route to an explicit wipe. */
    OrphanedData,
}
