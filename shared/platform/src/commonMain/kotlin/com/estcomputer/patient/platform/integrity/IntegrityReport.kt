package com.estcomputer.patient.platform.integrity

/** What the device checks found. These are hints, never proof, and nothing is ever blocked because of them. */
public data class IntegrityReport(
    val isRootSuspected: Boolean,
    val isDebuggable: Boolean,
    val isDebuggerAttached: Boolean,
) {
    public val hasConcern: Boolean get() = isRootSuspected || isDebuggable || isDebuggerAttached
}

public interface DeviceIntegrityChecker {
    public suspend fun check(): IntegrityReport
}
