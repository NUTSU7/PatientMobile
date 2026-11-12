package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.model.WipeStepResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransientFileCleanerTest {

    private val temp = AppDirectory.NO_BACKUP_FILES to "${AppPaths.TEMP}/draft.bin"
    private val share = AppDirectory.CACHE to "${AppPaths.SHARE}/report.pdf"
    private val keep = AppDirectory.NO_BACKUP_FILES to "${AppPaths.BLOBS}/a1"

    @Test
    fun clear_removesTempAndShareFilesOnly() = runTest {
        val files = FakeAppFileOps(temp, share, keep)

        val result = TransientFileCleaner(files).clear()

        assertEquals(WipeStepResult.DONE, result)
        assertEquals(setOf(keep), files.files)
    }

    @Test
    fun clear_whenTempIsLocked_stillClearsShareAndReportsFailure() = runTest {
        val files = FakeAppFileOps(temp, share)
        files.locked += temp

        val result = TransientFileCleaner(files).clear()

        assertEquals(WipeStepResult.FAILED, result)
        assertTrue(share !in files.files)
    }

    @Test
    fun clear_whenNothingExists_isDone() = runTest {
        assertEquals(WipeStepResult.DONE, TransientFileCleaner(FakeAppFileOps()).clear())
    }
}
