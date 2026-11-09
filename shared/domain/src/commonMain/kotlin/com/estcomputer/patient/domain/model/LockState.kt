package com.estcomputer.patient.domain.model

public sealed interface LockState {

    /** No vault exists yet, so the app must run first-time setup. */
    public data object Unconfigured : LockState

    /** [isRecoveryRequired] is true when the device key is gone and only the recovery words can unlock. */
    public data class Locked(val isRecoveryRequired: Boolean) : LockState

    public data object Unlocked : LockState
}
