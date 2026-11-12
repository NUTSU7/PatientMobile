package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.model.WipeResult
import com.estcomputer.patient.domain.model.WipeStepResult
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.port.ReminderCanceller
import com.estcomputer.patient.domain.port.StorageWiper
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class CrashException : RuntimeException()

private class Disk {
    var isMarkerPresent = false
    var hasFiles = true
    var hasKeys = true
    var pendingReminders = 3
}

class WipeAllDataTest {

    private val events = mutableListOf<String>()
    private val disk = Disk()
    private val keys = FakeKeyManager(events)
    private var crashAt: String? = null
    private var storageResult = WipeStepResult.DONE
    private var markerWriteResult = WipeStepResult.DONE
    private var markerDeleteResult = WipeStepResult.DONE

    private fun step(name: String, action: () -> Unit) {
        events += name
        if (crashAt == name) throw CrashException()
        action()
    }

    private val marker = object : WipeMarker {
        override suspend fun write(): WipeStepResult {
            step("marker.write") { if (markerWriteResult == WipeStepResult.DONE) disk.isMarkerPresent = true }
            return markerWriteResult
        }

        override suspend fun delete(): WipeStepResult {
            step("marker.delete") { if (markerDeleteResult == WipeStepResult.DONE) disk.isMarkerPresent = false }
            return markerDeleteResult
        }
    }

    private val storage = StorageWiper {
        step("storage.deleteAll") { if (storageResult == WipeStepResult.DONE) disk.hasFiles = false }
        storageResult
    }

    private val reminders = ReminderCanceller {
        step("reminders.cancelAll") { disk.pendingReminders = 0 }
    }

    private fun TestScope.wipe(): Pair<WipeAllData, SessionCoordinator> {
        val appScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val coordinator = SessionCoordinator(appScope, CrashingKeys(), emptyList())
        return WipeAllData(coordinator, CrashingKeys(), marker, storage, reminders) to coordinator
    }

    private inner class CrashingKeys : KeyManager by keys {
        override suspend fun lock() {
            if (crashAt == "keys.lock") throw CrashException()
            keys.lock()
        }

        override suspend fun destroyKeys(): KeyDestructionResult {
            if (crashAt == "keys.destroy") throw CrashException()
            disk.hasKeys = false
            return keys.destroyKeys()
        }
    }

    @Test
    fun invoke_runsStepsInOrderWithKeysBeforeFiles() = runTest {
        val (wipe, _) = wipe()

        wipe()

        assertEquals(
            listOf(
                "marker.write",
                "keys.lock",
                "keys.destroy",
                "storage.deleteAll",
                "reminders.cancelAll",
                "marker.delete",
                "keys.lock",
            ),
            events,
        )
    }

    @Test
    fun invoke_whenEverythingWorks_endsUnconfiguredWithNothingLeft() = runTest {
        val (wipe, coordinator) = wipe()

        val result = wipe()

        assertEquals(WipeResult.DONE, result)
        assertEquals(LockState.Unconfigured, coordinator.lockState.value)
        assertFalse(disk.isMarkerPresent)
        assertFalse(disk.hasFiles)
        assertFalse(disk.hasKeys)
        assertEquals(0, disk.pendingReminders)
    }

    @Test
    fun invoke_afterCrashAtAnyStep_rerunEndsClean() = runTest {
        val stepNames = listOf(
            "marker.write", "keys.lock", "keys.destroy", "storage.deleteAll", "reminders.cancelAll", "marker.delete",
        )
        for (crashPoint in stepNames) {
            resetDisk()
            crashAt = crashPoint
            val (crashing, _) = wipe()
            assertFailsWith<CrashException>(crashPoint) { crashing() }

            crashAt = null
            val (rerun, coordinator) = wipe()
            val result = rerun()

            assertEquals(WipeResult.DONE, result, crashPoint)
            assertEquals(LockState.Unconfigured, coordinator.lockState.value, crashPoint)
            assertFalse(disk.isMarkerPresent, crashPoint)
            assertFalse(disk.hasFiles, crashPoint)
            assertFalse(disk.hasKeys, crashPoint)
        }
    }

    @Test
    fun invoke_afterCrashPastMarkerWrite_leavesMarkerForStartup() = runTest {
        crashAt = "keys.destroy"
        val (wipe, _) = wipe()

        assertFailsWith<CrashException> { wipe() }

        assertTrue(disk.isMarkerPresent)
        assertTrue(disk.hasFiles)
    }

    @Test
    fun invoke_whenVaultFileRemains_keepsMarkerAndReportsIncomplete() = runTest {
        keys.destruction = KeyDestructionResult.VAULT_FILE_REMAINS
        val (wipe, coordinator) = wipe()

        val result = wipe()

        assertEquals(WipeResult.INCOMPLETE, result)
        assertTrue(disk.isMarkerPresent)
        assertFalse(disk.hasFiles)
        assertFalse(coordinator.lockState.value == LockState.Unconfigured)
    }

    @Test
    fun invoke_whenStorageFails_keepsMarkerAndReportsIncomplete() = runTest {
        storageResult = WipeStepResult.FAILED
        val (wipe, _) = wipe()

        val result = wipe()

        assertEquals(WipeResult.INCOMPLETE, result)
        assertTrue(disk.isMarkerPresent)
        assertTrue(disk.hasFiles)
        assertEquals(0, disk.pendingReminders)
    }

    @Test
    fun invoke_whenMarkerWriteFails_stillWipes() = runTest {
        markerWriteResult = WipeStepResult.FAILED
        val (wipe, _) = wipe()

        val result = wipe()

        assertEquals(WipeResult.DONE, result)
        assertFalse(disk.hasKeys)
        assertFalse(disk.hasFiles)
    }

    @Test
    fun invoke_whenMarkerDeleteFails_reportsIncomplete() = runTest {
        markerDeleteResult = WipeStepResult.FAILED
        val (wipe, coordinator) = wipe()

        assertEquals(WipeResult.INCOMPLETE, wipe())
        assertFalse(coordinator.lockState.value == LockState.Unconfigured)
    }

    @Test
    fun invoke_afterUnlockedSession_closesSessionBeforeDestroyingKeys() = runTest {
        val (wipe, coordinator) = wipe()
        coordinator.onUnlocked()

        wipe()

        assertNull(coordinator.unlockedSessionScope)
        assertTrue(events.indexOf("keys.lock") < events.indexOf("keys.destroy"))
    }

    private fun resetDisk() {
        events.clear()
        disk.isMarkerPresent = false
        disk.hasFiles = true
        disk.hasKeys = true
        disk.pendingReminders = 3
    }
}
