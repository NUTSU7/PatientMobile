package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.WipeStepResult
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidStorageHostTest {

    private val noBackup: File = Files.createTempDirectory("nobackup").toFile()
    private val cache: File = Files.createTempDirectory("cache").toFile()

    @AfterTest
    fun cleanUp() {
        noBackup.deleteRecursively()
        cache.deleteRecursively()
    }

    private fun kotlinx.coroutines.test.TestScope.dispatchers(): DispatcherProvider {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return object : DispatcherProvider {
            override val default = dispatcher
            override val io = dispatcher
            override val main = dispatcher
        }
    }

    private fun plant(root: File, path: String) {
        val file = File(root, path)
        file.parentFile?.mkdirs()
        file.writeText("x")
    }

    @Test
    fun deleteAll_afterPlantingEveryTarget_leavesNoAppFiles() = runTest {
        val planted = listOf(
            "patient.db", "patient.db-wal", "patient.db-shm", "patient.db-journal", "blobs/a/b", "tmp/t", "alarms.json",
        )
        planted.forEach { plant(noBackup, it) }
        plant(cache, "share/report.pdf")
        plant(noBackup, "vault.json")
        val wiper = AppStorageWiper(AndroidAppFileOps(noBackup, cache, dispatchers()))

        val result = wiper.deleteAll()

        assertEquals(WipeStepResult.DONE, result)
        assertEquals(listOf("vault.json"), noBackup.list()?.toList())
        assertFalse(File(cache, "share").exists())
    }

    @Test
    fun clearDirectory_keepsTheDirectory() = runTest {
        plant(cache, "share/report.pdf")
        val ops = AndroidAppFileOps(noBackup, cache, dispatchers())

        val result = ops.clearDirectory(AppDirectory.CACHE, "share")

        assertTrue(result)
        assertTrue(File(cache, "share").isDirectory)
        assertEquals(0, File(cache, "share").list()?.size)
    }

    @Test
    fun clearDirectory_whenMissing_isDone() = runTest {
        assertTrue(AndroidAppFileOps(noBackup, cache, dispatchers()).clearDirectory(AppDirectory.CACHE, "share"))
    }

    @Test
    fun marker_writeThenDelete_roundTrips() = runTest {
        val marker = AndroidWipeMarker(noBackup, dispatchers())

        assertEquals(WipeStepResult.DONE, marker.write())
        assertTrue(File(noBackup, AppPaths.WIPE_MARKER).exists())
        assertFalse(File(noBackup, "${AppPaths.WIPE_MARKER}.tmp").exists())

        assertEquals(WipeStepResult.DONE, marker.delete())
        assertFalse(File(noBackup, AppPaths.WIPE_MARKER).exists())
    }

    @Test
    fun marker_writeTwice_isFine() = runTest {
        val marker = AndroidWipeMarker(noBackup, dispatchers())

        marker.write()

        assertEquals(WipeStepResult.DONE, marker.write())
    }

    @Test
    fun marker_deleteWhenMissing_isDone() = runTest {
        assertEquals(WipeStepResult.DONE, AndroidWipeMarker(noBackup, dispatchers()).delete())
    }

    @Test
    fun marker_whenDirectoryIsGone_reportsFailed() = runTest {
        noBackup.deleteRecursively()

        assertEquals(WipeStepResult.FAILED, AndroidWipeMarker(noBackup, dispatchers()).write())
    }
}
