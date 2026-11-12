package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.BootInfo
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class BioPolicyTest {
    private val now = Instant.parse("2026-11-10T12:00:00Z")

    private class FakeBootInfo(val boot: Int) : BootInfo {
        override fun bootCount(): Int = boot

        override fun elapsedRealtimeMillis(): Long = 0
    }

    private class FakeClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private fun requiresPassword(boot: Int, unlockedAt: Instant, currentBoot: Int = 7): Boolean =
        BioPolicy(FakeBootInfo(currentBoot), FakeClock(now))
            .requiresPassword(BioPolicyData(boot, unlockedAt))

    @Test
    fun requiresPassword_recentUnlockSameBoot_isFalse() {
        assertFalse(requiresPassword(boot = 7, unlockedAt = now - 1.days))
    }

    @Test
    fun requiresPassword_unlockJustNow_isFalse() {
        assertFalse(requiresPassword(boot = 7, unlockedAt = now))
    }

    @Test
    fun requiresPassword_bootChanged_isTrue() {
        assertTrue(requiresPassword(boot = 6, unlockedAt = now - 1.days))
    }

    @Test
    fun requiresPassword_exactlySevenDays_isFalse() {
        assertFalse(requiresPassword(boot = 7, unlockedAt = now - 7.days))
    }

    @Test
    fun requiresPassword_overSevenDays_isTrue() {
        assertTrue(requiresPassword(boot = 7, unlockedAt = now - 7.days - 1.milliseconds))
    }

    @Test
    fun requiresPassword_unlockInFuture_isTrue() {
        assertTrue(requiresPassword(boot = 7, unlockedAt = now + 1.milliseconds))
    }
}
