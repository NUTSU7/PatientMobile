package com.estcomputer.patient.domain.model

/**
 * What the app found at launch, after any interrupted wipe was finished.
 * [isCleanupComplete] is false when a transient file could not be removed.
 */
public data class StartupResult(
    public val state: StartupState,
    public val isCleanupComplete: Boolean,
)
