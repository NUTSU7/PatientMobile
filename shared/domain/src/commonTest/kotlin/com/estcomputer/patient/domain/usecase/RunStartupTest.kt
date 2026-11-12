package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.WipeStepResult
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.port.ReminderCanceller
import com.estcomputer.patient.domain.port.StorageWiper
import com.estcomputer.patient.domain.port.TransientFiles
import com.estcomputer.patient.domain.port.WipeMarker
import com.estcomputer.patient.domain.session.FakeKeyManager
import com.estcomputer.patient.domain.session.SessionCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RunStartupTest {

    private val events = mutableListOf<String>()
    private val fakeKeys = FakeKeyManager(events)
    private val states = ArrayDeque<StartupState>()
    private var destruction = KeyDestructionResult.DONE
    private var transientResult = WipeStepResult.DONE

    private val keys = object : KeyManager by fakeKeys {
        override suspend fun startupState(): StartupState {
            events += "keys.startupState"
            return states.removeFirst()
        }

        override suspend fun destroyKeys(): KeyDestructionResult {
            events += "keys.destroy"
            return destruction
        }
    }

    private val marker = object : WipeMarker {
        override suspend fun write(): WipeStepResult = WipeStepResult.DONE.also { events += "marker.write" }
        override suspend fun delete(): WipeStepResult = WipeStepResult.DONE.also { events += "marker.delete" }
    }

    private val transientFiles = TransientFiles {
        events += "transient.clear"
        transientResult
    }

    private fun TestScope.startup(): Pair<RunStartup, SessionCoordinator> {
        val appScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val coordinator = SessionCoordinator(appScope, keys, emptyList())
        val wipe = WipeAllData(
            coordinator,
            keys,
            marker,
            StorageWiper { WipeStepResult.DONE.also { events += "storage.deleteAll" } },
            ReminderCanceller { events += "reminders.cancelAll" },
        )
        return RunStartup(keys, wipe, transientFiles, coordinator) to coordinator
    }

    @Test
    fun invoke_whenCleanInstall_publishesUnconfigured() = runTest {
        states += StartupState.Clean
        val (startup, coordinator) = startup()

        val result = startup()

        assertEquals(StartupState.Clean, result.state)
        assertTrue(result.isCleanupComplete)
        assertEquals(LockState.Unconfigured, coordinator.lockState.value)
    }

    @Test
    fun invoke_whenVaultPresent_publishesLockedWithoutRecovery() = runTest {
        states += StartupState.VaultPresent
        val (startup, coordinator) = startup()

        startup()

        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
    }

    @Test
    fun invoke_whenWipeInterrupted_finishesWipeBeforeClearingTransientFiles() = runTest {
        states += StartupState.WipeInterrupted
        states += StartupState.Clean
        val (startup, coordinator) = startup()

        val result = startup()

        assertEquals(StartupState.Clean, result.state)
        assertEquals(LockState.Unconfigured, coordinator.lockState.value)
        assertTrue(events.indexOf("keys.destroy") < events.indexOf("transient.clear"))
        assertTrue(events.indexOf("marker.delete") < events.indexOf("transient.clear"))
    }

    @Test
    fun invoke_whenInterruptedWipeStaysIncomplete_keepsInterruptedState() = runTest {
        states += StartupState.WipeInterrupted
        destruction = KeyDestructionResult.VAULT_FILE_REMAINS
        val (startup, coordinator) = startup()

        val result = startup()

        assertEquals(StartupState.WipeInterrupted, result.state)
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
        assertFalse(events.contains("marker.delete"))
    }

    @Test
    fun invoke_whenOrphanedData_doesNotPublishAnIdleState() = runTest {
        states += StartupState.OrphanedData
        val (startup, coordinator) = startup()

        val result = startup()

        assertEquals(StartupState.OrphanedData, result.state)
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
    }

    @Test
    fun invoke_whenTransientFilesRemain_reportsIncompleteCleanup() = runTest {
        states += StartupState.VaultPresent
        transientResult = WipeStepResult.FAILED
        val (startup, _) = startup()

        val result = startup()

        assertFalse(result.isCleanupComplete)
    }

    @Test
    fun invoke_onEveryPath_clearsTransientFilesExactlyOnce() = runTest {
        for (state in StartupState.entries) {
            events.clear()
            states.clear()
            states += state
            states += StartupState.Clean
            val (startup, _) = startup()

            startup()

            assertEquals(1, events.count { it == "transient.clear" }, state.name)
        }
    }
}
