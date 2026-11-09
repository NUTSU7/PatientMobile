package com.estcomputer.patient.domain.model

/** Setup prepared in memory only; nothing is on disk until [commit]. */
public interface PendingSetup {
    /** The 24 recovery words to show the patient. Zeroed by [commit] and [abandon]. */
    public val recoveryWords: SecretChars

    /** Writes the vault and unlocks. Call only after the patient confirmed the words. */
    public suspend fun commit(): SetupCommitResult

    /** Zeroes everything; nothing was written. Safe to call more than once. */
    public fun abandon()
}
