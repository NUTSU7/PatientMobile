package com.estcomputer.patient.platform.autolock

import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.session.SessionCoordinator
import com.estcomputer.patient.platform.support.FakeKeyManager
import com.estcomputer.patient.platform.support.SchedulerBootInfo
import com.estcomputer.patient.platform.support.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val MINUTE = 60_000L

class AutoLockPolicyTest {

    private class Rig(val scope: TestScope, settings: AutoLockSettings) {
        val dispatcher = StandardTestDispatcher(scope.testScheduler)
        val clock = SchedulerBootInfo(scope.testScheduler)
        val keys = FakeKeyManager()
        val coordinator = SessionCoordinator(scope.backgroundScope, keys, emptyList())
        val policy = AutoLockPolicy(
            coordinator, clock, TestDispatchers(dispatcher), scope.backgroundScope, settings,
        )

        suspend fun unlock() {
            policy.start()
            coordinator.onUnlocked(coordinator.startUnlock())
            scope.runCurrent()
        }

        val isLocked: Boolean get() = coordinator.lockState.value != LockState.Unlocked

        fun advance(millis: Long) {
            scope.advanceTimeBy(millis)
            scope.runCurrent()
        }
    }

    private fun TestScope.rig(settings: AutoLockSettings = AutoLockSettings()) = Rig(this, settings)

    @Test
    fun inactivity_beforeTheLimit_staysUnlocked() = runTest {
        val rig = rig().also { it.unlock() }

        rig.advance(5 * MINUTE - 1)

        assertEquals(LockState.Unlocked, rig.coordinator.lockState.value)
    }

    @Test
    fun inactivity_atTheLimit_locks() = runTest {
        val rig = rig().also { it.unlock() }

        rig.advance(5 * MINUTE)

        assertEquals(true, rig.isLocked)
        assertEquals(1, rig.keys.lockCalls)
    }

    @Test
    fun userInteraction_pushesTheDeadlineOut() = runTest {
        val rig = rig().also { it.unlock() }
        rig.advance(4 * MINUTE)
        rig.policy.onUserInteraction()

        rig.advance(4 * MINUTE)
        assertEquals(LockState.Unlocked, rig.coordinator.lockState.value)

        rig.advance(MINUTE)
        assertEquals(true, rig.isLocked)
    }

    @Test
    fun manyInteractions_doNotMultiplyTimers() = runTest {
        val rig = rig().also { it.unlock() }

        repeat(1_000) { rig.policy.onUserInteraction() }
        rig.advance(5 * MINUTE)

        assertEquals(1, rig.keys.lockCalls)
    }

    @Test
    fun settings_stricterThanDefault_areHonoured() = runTest {
        val rig = rig(AutoLockSettings(inactivityMillis = MINUTE, backgroundMillis = 10_000)).also { it.unlock() }

        rig.advance(MINUTE)

        assertEquals(true, rig.isLocked)
    }

    @Test
    fun settings_longerThanDefault_fallBackToDefault() {
        val clamped = AutoLockSettings(inactivityMillis = 30 * MINUTE, backgroundMillis = 10 * MINUTE).clamped()

        assertEquals(AutoLockSettings(), clamped)
    }

    @Test
    fun settings_notPositive_fallBackToDefault() {
        assertEquals(AutoLockSettings(), AutoLockSettings(inactivityMillis = 0, backgroundMillis = -5).clamped())
    }

    @Test
    fun background_locksAfterOneMinute() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.onBackground()

        rig.advance(MINUTE - 1)
        assertEquals(LockState.Unlocked, rig.coordinator.lockState.value)

        rig.advance(1)
        assertEquals(true, rig.isLocked)
    }

    @Test
    fun foreground_beforeTheLimit_keepsSessionAndRestartsInactivity() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.onBackground()
        rig.advance(30_000)

        rig.policy.onForeground()
        rig.advance(4 * MINUTE)

        assertEquals(LockState.Unlocked, rig.coordinator.lockState.value)
        rig.advance(MINUTE)
        assertEquals(true, rig.isLocked)
    }

    @Test
    fun foreground_afterFrozenProcess_locksWithoutTheTimer() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.onBackground()
        rig.clock.frozenMillis = 2 * MINUTE

        rig.policy.onForeground()
        rig.scope.runCurrent()

        assertEquals(true, rig.isLocked)
    }

    @Test
    fun interaction_whileInBackground_isIgnored() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.onBackground()
        rig.advance(30_000)

        rig.policy.onUserInteraction()
        rig.advance(30_000)

        assertEquals(true, rig.isLocked)
    }

    @Test
    fun screenOff_locksAtOnce() = runTest {
        val rig = rig().also { it.unlock() }

        rig.policy.onScreenOff()
        rig.scope.runCurrent()

        assertEquals(true, rig.isLocked)
    }

    @Test
    fun externalActivity_holdsTheBackgroundLock() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.setExternalActivityInProgress(true)
        rig.policy.onBackground()

        rig.advance(9 * MINUTE)

        assertEquals(LockState.Unlocked, rig.coordinator.lockState.value)
    }

    @Test
    fun externalActivity_expiresAfterTenMinutesAndThenLocks() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.setExternalActivityInProgress(true)
        rig.policy.onBackground()

        rig.advance(10 * MINUTE)

        assertEquals(true, rig.isLocked)
    }

    @Test
    fun externalActivity_returnAfterLongerThanBackgroundLimit_keepsSession() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.setExternalActivityInProgress(true)
        rig.policy.onBackground()
        rig.advance(3 * MINUTE)

        rig.policy.onForeground()
        rig.policy.setExternalActivityInProgress(false)
        rig.advance(MINUTE)

        assertEquals(LockState.Unlocked, rig.coordinator.lockState.value)
    }

    @Test
    fun externalActivity_clearedWhileStillInBackgroundPastLimit_locks() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.setExternalActivityInProgress(true)
        rig.policy.onBackground()
        rig.advance(3 * MINUTE)

        rig.policy.setExternalActivityInProgress(false)
        rig.scope.runCurrent()

        assertEquals(true, rig.isLocked)
    }

    @Test
    fun screenOff_duringExternalActivity_stillLocks() = runTest {
        val rig = rig().also { it.unlock() }
        rig.policy.setExternalActivityInProgress(true)
        rig.policy.onBackground()

        rig.policy.onScreenOff()
        rig.scope.runCurrent()

        assertEquals(true, rig.isLocked)
    }

    @Test
    fun timers_whileLocked_doNothing() = runTest {
        val rig = rig()
        rig.policy.start()
        rig.scope.runCurrent()

        rig.policy.onBackground()
        rig.advance(10 * MINUTE)

        assertEquals(0, rig.keys.lockCalls)
    }

    @Test
    fun nextUnlock_startsFreshInactivityWindow() = runTest {
        val rig = rig().also { it.unlock() }
        rig.advance(5 * MINUTE)
        assertEquals(true, rig.isLocked)
        rig.advance(2 * MINUTE)

        rig.unlock()
        rig.advance(4 * MINUTE)

        assertEquals(LockState.Unlocked, rig.coordinator.lockState.value)
        rig.advance(MINUTE)
        assertNotEquals(LockState.Unlocked, rig.coordinator.lockState.value)
    }
}
