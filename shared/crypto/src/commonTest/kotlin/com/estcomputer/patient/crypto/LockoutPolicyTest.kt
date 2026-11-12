package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.BootInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class LockoutPolicyTest {
    private class FakeBootInfo(var boot: Int, var elapsed: Long) : BootInfo {
        override fun bootCount(): Int = boot

        override fun elapsedRealtimeMillis(): Long = elapsed
    }

    private fun vault(attempts: Int = 0, until: Long = 0, boot: Int = 1): VaultFile =
        VaultFile(
            format = CryptoConstants.VAULT_FORMAT,
            createdAt = "2026-01-01T00:00:00Z",
            kdf = VaultFile.Kdf("argon2id", ByteArray(CryptoConstants.SALT_BYTES), 65_536, 3, 1),
            hwKeyAlias = CryptoConstants.HW_KEY_ALIAS,
            wrappedMkPassword = ByteArray(88),
            wrappedMkRecovery = ByteArray(60),
            wrappedMkBiometric = null,
            bioKeyAlias = null,
            exportPublicKey = ByteArray(CryptoConstants.KEY_SIZE_BYTES),
            failedAttempts = attempts,
            wipeAfterFailedAttempts = 0,
            lockoutUntilElapsed = until,
            lockoutBootCount = boot,
        )

    @Test
    fun delayFor_counts1To12_followsSchedule() {
        val expected = listOf(
            Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO,
            30.seconds, 1.minutes, 5.minutes, 15.minutes, 1.hours, 1.hours, 1.hours, 1.hours,
        )

        val actual = (1..12).map(LockoutPolicy::delayFor)

        assertEquals(expected, actual)
    }

    @Test
    fun delayFor_zeroAttempts_isZero() {
        assertEquals(Duration.ZERO, LockoutPolicy.delayFor(0))
    }

    @Test
    fun status_beforeDeadline_isActiveWithRemaining() {
        val boot = FakeBootInfo(boot = 1, elapsed = 10_000)

        val status = LockoutPolicy.status(vault(attempts = 5, until = 40_000), boot)

        assertEquals(LockoutStatus.Active(30.seconds), status)
    }

    @Test
    fun status_atOrAfterDeadline_isNone() {
        val boot = FakeBootInfo(boot = 1, elapsed = 40_000)
        val locked = vault(attempts = 5, until = 40_000)

        assertEquals(LockoutStatus.None, LockoutPolicy.status(locked, boot))
        boot.elapsed = Long.MAX_VALUE
        assertEquals(LockoutStatus.None, LockoutPolicy.status(locked, boot))
    }

    @Test
    fun status_deadlineFarAhead_isClampedToOneHour() {
        val boot = FakeBootInfo(boot = 1, elapsed = 0)

        val status = LockoutPolicy.status(vault(attempts = 9, until = Long.MAX_VALUE), boot)

        assertEquals(LockoutStatus.Active(1.hours), status)
    }

    @Test
    fun status_afterReboot_armsFullDelay() {
        val boot = FakeBootInfo(boot = 2, elapsed = 1_000)

        val status = LockoutPolicy.status(vault(attempts = 7, until = 999_999, boot = 1), boot)

        assertEquals(LockoutStatus.Active(5.minutes), status)
    }

    @Test
    fun status_afterRebootBelowThreshold_isNone() {
        val boot = FakeBootInfo(boot = 2, elapsed = 1_000)

        assertEquals(LockoutStatus.None, LockoutPolicy.status(vault(attempts = 4, boot = 1), boot))
    }

    @Test
    fun rearmedAfterReboot_bootChanged_storesDeadlineAndBootCount() {
        val boot = FakeBootInfo(boot = 2, elapsed = 1_000)

        val rearmed = LockoutPolicy.rearmedAfterReboot(vault(attempts = 6, until = 5, boot = 1), boot)

        assertEquals(1_000 + 60_000, rearmed?.lockoutUntilElapsed)
        assertEquals(2, rearmed?.lockoutBootCount)
    }

    @Test
    fun rearmedAfterReboot_sameBoot_returnsNull() {
        val boot = FakeBootInfo(boot = 1, elapsed = 1_000)

        assertNull(LockoutPolicy.rearmedAfterReboot(vault(attempts = 6, until = 5, boot = 1), boot))
    }

    @Test
    fun rearmedAfterReboot_noDelayOwed_returnsNull() {
        val boot = FakeBootInfo(boot = 2, elapsed = 1_000)

        assertNull(LockoutPolicy.rearmedAfterReboot(vault(attempts = 3, boot = 1), boot))
    }

    @Test
    fun status_afterRearmPersisted_countsDownFromNow() {
        val boot = FakeBootInfo(boot = 2, elapsed = 1_000)
        val rearmed = requireNotNull(LockoutPolicy.rearmedAfterReboot(vault(attempts = 6, boot = 1), boot))
        boot.elapsed = 31_000

        assertEquals(LockoutStatus.Active(30.seconds), LockoutPolicy.status(rearmed, boot))
    }
}
