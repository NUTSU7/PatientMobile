package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.BootInfo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal sealed interface LockoutStatus {
    data object None : LockoutStatus

    data class Active(val remaining: Duration) : LockoutStatus
}

internal object LockoutPolicy {
    /** Zero below the first scheduled count; counts above the last scheduled one use its delay. */
    fun delayFor(failedAttempts: Int): Duration =
        CryptoConstants.LOCKOUT_SCHEDULE.entries
            .filter { it.key <= failedAttempts }
            .maxByOrNull { it.key }
            ?.value
            ?: Duration.ZERO

    fun status(vault: VaultFile, bootInfo: BootInfo): LockoutStatus {
        val delay = delayFor(vault.failedAttempts)
        val remaining = if (vault.lockoutBootCount == bootInfo.bootCount()) {
            // A stored deadline beyond the longest delay is tampering or overflow, so it is capped.
            minOf(
                (vault.lockoutUntilElapsed - bootInfo.elapsedRealtimeMillis()).milliseconds,
                MAX_DELAY,
            )
        } else {
            delay
        }
        return if (remaining > Duration.ZERO) LockoutStatus.Active(remaining) else LockoutStatus.None
    }

    /**
     * D3: after a reboot the elapsed clock restarts, so the stored deadline means nothing. Returns the vault
     * to persist with the full delay armed from now, or null when nothing needs writing.
     */
    fun rearmedAfterReboot(vault: VaultFile, bootInfo: BootInfo): VaultFile? {
        val bootCount = bootInfo.bootCount()
        val delay = delayFor(vault.failedAttempts)
        val isRearmNeeded = vault.lockoutBootCount != bootCount && delay > Duration.ZERO
        return if (isRearmNeeded) {
            vault.copy(
                lockoutUntilElapsed = bootInfo.elapsedRealtimeMillis() + delay.inWholeMilliseconds,
                lockoutBootCount = bootCount,
            )
        } else {
            null
        }
    }

    private val MAX_DELAY: Duration = CryptoConstants.LOCKOUT_SCHEDULE.values.max()
}
