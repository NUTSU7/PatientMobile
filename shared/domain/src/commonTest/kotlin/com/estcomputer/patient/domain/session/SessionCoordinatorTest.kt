package com.estcomputer.patient.domain.session

import app.cash.turbine.test
import com.estcomputer.patient.domain.model.LockState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionCoordinatorTest {

    private val events = mutableListOf<String>()
    private val keys = FakeKeyManager(events)

    private fun TestScope.coordinator(vararg closers: SessionCloser): SessionCoordinator {
        val appScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        return SessionCoordinator(appScope, keys, closers.toList())
    }

    private fun recordingCloser(name: String) = SessionCloser { events += "closer.$name" }

    @Test
    fun lockState_initially_isLockedWithoutRecovery() = runTest {
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator().lockState.value)
    }

    @Test
    fun onUnlocked_opensScopeAndPublishesUnlocked() = runTest {
        val coordinator = coordinator()

        coordinator.onUnlocked(coordinator.startUnlock())

        assertEquals(LockState.Unlocked, coordinator.lockState.value)
        assertTrue(coordinator.unlockedSessionScope?.coroutineContext?.get(Job)?.isActive == true)
    }

    @Test
    fun onUnlocked_afterLockSinceTicket_zeroesKeysAgainAndStaysLocked() = runTest {
        val coordinator = coordinator()
        coordinator.setIdleState(LockState.Unconfigured)
        val ticket = coordinator.startUnlock()
        coordinator.lock()
        events.clear()

        val isOpened = coordinator.onUnlocked(ticket)

        assertFalse(isOpened)
        assertEquals(listOf("keys.lock"), events)
        assertNull(coordinator.unlockedSessionScope)
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
    }

    @Test
    fun onUnlocked_withCurrentTicket_returnsTrue() = runTest {
        val coordinator = coordinator()

        assertTrue(coordinator.onUnlocked(coordinator.startUnlock()))
    }

    @Test
    fun lock_cancelsSessionJobsBeforeClosersRun() = runTest {
        val coordinator = coordinator(recordingCloser("db"))
        coordinator.onUnlocked(coordinator.startUnlock())
        val started = CompletableDeferred<Unit>()
        requireNotNull(coordinator.unlockedSessionScope).launch {
            try {
                started.complete(Unit)
                awaitCancellation()
            } finally {
                events += "job.cancelled"
            }
        }
        started.await()

        coordinator.lock()

        assertEquals(listOf("job.cancelled", "closer.db", "keys.lock"), events)
    }

    @Test
    fun lock_whenJobIgnoresCancellation_zeroesKeysAfterTimeout() = runTest {
        val coordinator = coordinator(recordingCloser("db"))
        coordinator.onUnlocked(coordinator.startUnlock())
        val started = CompletableDeferred<Unit>()
        val stuck = requireNotNull(coordinator.unlockedSessionScope).launch {
            withContext(NonCancellable) {
                started.complete(Unit)
                awaitCancellation()
            }
        }
        started.await()

        coordinator.lock()

        assertEquals(listOf("closer.db", "keys.lock"), events)
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
        // The job is still running: the lock did not wait for it.
        assertFalse(stuck.isCompleted)
    }

    @Test
    fun lock_runsClosersInOrderBeforeZeroingKeys() = runTest {
        val coordinator = coordinator(recordingCloser("first"), recordingCloser("second"))
        coordinator.onUnlocked(coordinator.startUnlock())

        coordinator.lock()

        assertEquals(listOf("closer.first", "closer.second", "keys.lock"), events)
    }

    @Test
    fun lock_whenCloserFails_stillRunsOthersAndZeroesKeys() = runTest {
        val failing = SessionCloser { error("closer failed") }
        val coordinator = coordinator(failing, recordingCloser("after"))
        coordinator.onUnlocked(coordinator.startUnlock())

        coordinator.lock()

        assertEquals(listOf("closer.after", "keys.lock"), events)
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
    }

    @Test
    fun lock_publishesLockedBeforeClosersRun() = runTest {
        val seen = mutableListOf<LockState>()
        lateinit var coordinator: SessionCoordinator
        coordinator = coordinator(SessionCloser { seen += coordinator.lockState.value })
        coordinator.onUnlocked(coordinator.startUnlock())

        coordinator.lock()

        assertEquals(listOf<LockState>(LockState.Locked(isRecoveryRequired = false)), seen)
    }

    @Test
    fun lock_twice_runsClosersOnce() = runTest {
        val coordinator = coordinator(recordingCloser("db"))
        coordinator.onUnlocked(coordinator.startUnlock())

        coordinator.lock()
        coordinator.lock()

        assertEquals(1, events.count { it == "closer.db" })
    }

    @Test
    fun lock_withoutSession_stillZeroesKeys() = runTest {
        val coordinator = coordinator(recordingCloser("db"))

        coordinator.lock()

        assertEquals(listOf("keys.lock"), events)
    }

    @Test
    fun lock_clearsSessionScope() = runTest {
        val coordinator = coordinator()
        coordinator.onUnlocked(coordinator.startUnlock())

        coordinator.lock()

        assertNull(coordinator.unlockedSessionScope)
    }

    @Test
    fun onUnlocked_afterLock_createsNewScope() = runTest {
        val coordinator = coordinator()
        coordinator.onUnlocked(coordinator.startUnlock())
        val first = assertNotNull(coordinator.unlockedSessionScope)
        coordinator.lock()

        coordinator.onUnlocked(coordinator.startUnlock())

        val second = assertNotNull(coordinator.unlockedSessionScope)
        assertNotSame(first, second)
        assertFalse(first.coroutineContext[Job]?.isActive == true)
        assertTrue(second.coroutineContext[Job]?.isActive == true)
    }

    @Test
    fun lockState_emitsUnlockedThenLocked() = runTest {
        val coordinator = coordinator()

        coordinator.lockState.test {
            assertEquals(LockState.Locked(isRecoveryRequired = false), awaitItem())
            coordinator.onUnlocked(coordinator.startUnlock())
            assertEquals(LockState.Unlocked, awaitItem())
            coordinator.lock()
            assertEquals(LockState.Locked(isRecoveryRequired = false), awaitItem())
        }
    }

    @Test
    fun setIdleState_whileLocked_publishesIt() = runTest {
        val coordinator = coordinator()

        coordinator.setIdleState(LockState.Locked(isRecoveryRequired = true))

        assertEquals(LockState.Locked(isRecoveryRequired = true), coordinator.lockState.value)
    }

    @Test
    fun setIdleState_whileUnlocked_isIgnored() = runTest {
        val coordinator = coordinator()
        coordinator.onUnlocked(coordinator.startUnlock())

        coordinator.setIdleState(LockState.Unconfigured)

        assertEquals(LockState.Unlocked, coordinator.lockState.value)
    }

    @Test
    fun onWiped_locksThenPublishesUnconfigured() = runTest {
        val coordinator = coordinator(recordingCloser("db"))
        coordinator.onUnlocked(coordinator.startUnlock())

        coordinator.onWiped()

        assertEquals(listOf("closer.db", "keys.lock"), events)
        assertEquals(LockState.Unconfigured, coordinator.lockState.value)
        assertNull(coordinator.unlockedSessionScope)
    }
}
