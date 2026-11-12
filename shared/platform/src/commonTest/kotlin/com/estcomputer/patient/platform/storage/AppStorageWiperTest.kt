package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.model.WipeStepResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppStorageWiperTest {

    private val nb = AppDirectory.NO_BACKUP_FILES
    private val cache = AppDirectory.CACHE

    private fun populated() = FakeAppFileOps(
        nb to AppPaths.DATABASE,
        nb to AppPaths.DATABASE_WAL,
        nb to AppPaths.DATABASE_SHM,
        nb to AppPaths.DATABASE_JOURNAL,
        nb to "${AppPaths.BLOBS}/abc",
        nb to "${AppPaths.BLOBS}/def",
        nb to "${AppPaths.TEMP}/x",
        nb to AppPaths.ALARMS,
        cache to "${AppPaths.SHARE}/report.pdf",
    )

    @Test
    fun deleteAll_removesEveryTarget() = runTest {
        val ops = populated()

        val result = AppStorageWiper(ops).deleteAll()

        assertEquals(WipeStepResult.DONE, result)
        assertTrue(ops.files.isEmpty())
    }

    @Test
    fun deleteAll_keepsFilesOutsideTheTargets() = runTest {
        val ops = populated()
        ops.files += nb to "vault.json"

        AppStorageWiper(ops).deleteAll()

        assertEquals(setOf(nb to "vault.json"), ops.files)
    }

    @Test
    fun deleteAll_whenNothingExists_isDone() = runTest {
        assertEquals(WipeStepResult.DONE, AppStorageWiper(FakeAppFileOps()).deleteAll())
    }

    @Test
    fun deleteAll_whenOneTargetIsLocked_triesTheRestAndReportsFailed() = runTest {
        val ops = populated()
        ops.locked += nb to "${AppPaths.BLOBS}/abc"
        val wiper = AppStorageWiper(ops)

        val report = wiper.deleteTargets()

        assertEquals(setOf(WipeTarget.BLOBS), report.failed)
        assertTrue(ops.files.contains(nb to "${AppPaths.BLOBS}/abc"))
        assertTrue(ops.files.none { it.second == AppPaths.DATABASE || it.second == AppPaths.ALARMS })
        assertTrue((nb to "${AppPaths.TEMP}/x") !in ops.files)
    }

    @Test
    fun deleteAll_whenOneTargetIsLocked_returnsFailed() = runTest {
        val ops = populated()
        ops.locked += nb to AppPaths.DATABASE

        assertEquals(WipeStepResult.FAILED, AppStorageWiper(ops).deleteAll())
    }

    @Test
    fun deleteAll_calledAgainAfterFailure_converges() = runTest {
        val ops = populated()
        ops.locked += nb to AppPaths.DATABASE
        val wiper = AppStorageWiper(ops)
        wiper.deleteAll()

        ops.locked.clear()

        assertEquals(WipeStepResult.DONE, wiper.deleteAll())
        assertTrue(ops.files.isEmpty())
    }

    @Test
    fun shareCacheCleaner_close_emptiesShareDirectoryOnly() = runTest {
        val ops = populated()

        ShareCacheCleaner(ops).close()

        assertTrue(ops.files.none { it.first == cache })
        assertTrue(ops.files.contains(nb to AppPaths.DATABASE))
    }

    @Test
    fun wipeTargets_coverEveryNameInAppPaths() {
        val names = WipeTarget.entries.map { it.relativePath }.toSet()

        assertEquals(
            setOf(
                AppPaths.DATABASE, AppPaths.DATABASE_WAL, AppPaths.DATABASE_SHM, AppPaths.DATABASE_JOURNAL,
                AppPaths.BLOBS, AppPaths.TEMP, AppPaths.ALARMS, AppPaths.SHARE,
            ),
            names,
        )
    }
}
