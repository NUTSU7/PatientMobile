package com.estcomputer.patient.platform.storage

import androidx.test.platform.app.InstrumentationRegistry
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.WipeStepResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AndroidWipeDeviceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val dispatchers = object : DispatcherProvider {
        override val default = Dispatchers.Default
        override val io = Dispatchers.IO
        override val main = Dispatchers.Main
    }

    private val keptFile = File(context.noBackupFilesDir, "kept-by-test.bin")

    @After
    fun tearDown() {
        keptFile.delete()
        File(context.noBackupFilesDir, AppPaths.WIPE_MARKER).delete()
    }

    private fun plant(root: File, path: String) {
        val file = File(root, path)
        file.parentFile?.mkdirs()
        file.writeText("x")
    }

    @Test
    fun wipe_afterPlantingEveryTarget_leavesNoAppFiles() = runBlocking {
        val noBackup = context.noBackupFilesDir
        listOf(AppPaths.DATABASE, AppPaths.DATABASE_WAL, AppPaths.DATABASE_SHM, AppPaths.DATABASE_JOURNAL)
            .plus(listOf(AppPaths.ALARMS, "${AppPaths.TEMP}/t"))
            .forEach { plant(noBackup, it) }
        plant(noBackup, "${AppPaths.BLOBS}/a/b")
        plant(context.cacheDir, "${AppPaths.SHARE}/report.pdf")
        keptFile.writeText("x")
        val marker = AndroidWipeMarker(noBackup, dispatchers)
        assertEquals(WipeStepResult.DONE, marker.write())

        val result = AppStorageWiper(AndroidAppFileOps(noBackup, context.cacheDir, dispatchers)).deleteAll()
        marker.delete()

        assertEquals(WipeStepResult.DONE, result)
        WipeTarget.entries.forEach { target ->
            val root = if (target.directory == AppDirectory.CACHE) context.cacheDir else noBackup
            assertFalse(File(root, target.relativePath).exists(), target.name)
        }
        assertFalse(File(noBackup, AppPaths.WIPE_MARKER).exists())
        assertEquals(true, keptFile.exists())
    }
}
