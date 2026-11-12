package com.estcomputer.patient.domain.model

import com.estcomputer.patient.domain.port.BiometricChallenge

public sealed interface UnlockResult {
    /** [needsBiometricRefresh] is true after a password unlock with biometrics enabled. */
    public data class Unlocked(val needsBiometricRefresh: Boolean) : UnlockResult

    public data class Failed(val error: KeyError) : UnlockResult

    public data class PasswordRejected(val reason: PasswordRejection) : UnlockResult

    public data class LockedOut(val remainingMillis: Long) : UnlockResult

    /** The failed-attempt limit was reached and all data was destroyed. */
    public data object Wiped : UnlockResult
}

public sealed interface SetupResult {
    public class Ready(public val pending: PendingSetup) : SetupResult {
        override fun toString(): String = "Ready"
    }

    public data class PasswordRejected(val reason: PasswordRejection) : SetupResult

    public data class Failed(val error: KeyError) : SetupResult
}

public sealed interface SetupCommitResult {
    public data object Committed : SetupCommitResult

    public data class Failed(val error: KeyError) : SetupCommitResult
}

public sealed interface ChangePasswordResult {
    public data object Changed : ChangePasswordResult

    public data class PasswordRejected(val reason: PasswordRejection) : ChangePasswordResult

    public data class Failed(val error: KeyError) : ChangePasswordResult

    public data class LockedOut(val remainingMillis: Long) : ChangePasswordResult

    /** The old password failed at the auto-wipe limit and all data was destroyed. */
    public data object Wiped : ChangePasswordResult
}

public sealed interface BiometricBeginResult {
    public class Ready(public val challenge: BiometricChallenge) : BiometricBeginResult {
        override fun toString(): String = "Ready"
    }

    public data class Failed(val error: KeyError) : BiometricBeginResult
}

public sealed interface BiometricEnrolmentResult {
    public data object Enabled : BiometricEnrolmentResult

    public data class Failed(val error: KeyError) : BiometricEnrolmentResult
}
