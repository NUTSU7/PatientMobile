package com.estcomputer.patient.platform.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppDataInspectorTest {

    private val noBackup = AppDirectory.NO_BACKUP_FILES

    @Test
    fun hasWipeMarker_whenMarkerPresent_isTrue() = runTest {
        val inspector = AppDataInspector(FakeAppFileOps(noBackup to AppPaths.WIPE_MARKER))

        assertTrue(inspector.hasWipeMarker())
    }

    @Test
    fun hasWipeMarker_whenOnlyAStagingFileExists_isFalse() = runTest {
        val inspector = AppDataInspector(FakeAppFileOps(noBackup to "${AppPaths.WIPE_MARKER}.tmp"))

        assertFalse(inspector.hasWipeMarker())
    }

    @Test
    fun hasAppData_forEachDataPath_isTrue() = runTest {
        val paths = listOf(AppPaths.DATABASE, AppPaths.BLOBS, AppPaths.ALARMS)
        for (path in paths) {
            val inspector = AppDataInspector(FakeAppFileOps(noBackup to path))

            assertTrue(inspector.hasAppData(), path)
        }
    }

    @Test
    fun hasAppData_whenBlobsHoldFiles_isTrue() = runTest {
        val inspector = AppDataInspector(FakeAppFileOps(noBackup to "${AppPaths.BLOBS}/a1"))

        assertTrue(inspector.hasAppData())
    }

    @Test
    fun hasAppData_whenOnlyTransientFilesExist_isFalse() = runTest {
        val files = FakeAppFileOps(noBackup to "${AppPaths.TEMP}/x", AppDirectory.CACHE to "${AppPaths.SHARE}/y.pdf")

        assertFalse(AppDataInspector(files).hasAppData())
    }

    @Test
    fun hasAppData_whenEmpty_isFalse() = runTest {
        assertEquals(false, AppDataInspector(FakeAppFileOps()).hasAppData())
    }
}
