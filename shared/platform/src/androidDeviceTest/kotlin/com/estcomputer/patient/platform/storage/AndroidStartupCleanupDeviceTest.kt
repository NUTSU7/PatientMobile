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
import kotlin.test.assertTrue

class AndroidStartupCleanupDeviceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val dispatchers = object : DispatcherProvider {
        override val default = Dispatchers.Default
        override val io = Dispatchers.IO
        override val main = Dispatchers.Main
    }

    private val keptFile = File(context.noBackupFilesDir, "${AppPaths.BLOBS}/kept-by-test.bin")

    @After
    fun tearDown() {
        keptFile.delete()
        File(context.noBackupFilesDir, AppPaths.TEMP).deleteRecursively()
        File(context.cacheDir, AppPaths.SHARE).deleteRecursively()
    }

    private fun plant(file: File) {
        file.parentFile?.mkdirs()
        file.writeText("x")
    }

    @Test
    fun clear_afterPlantingTempAndShareFiles_removesThemAndKeepsBlobs() = runBlocking {
        val temp = File(context.noBackupFilesDir, "${AppPaths.TEMP}/sub/draft.bin")
        val shared = File(context.cacheDir, "${AppPaths.SHARE}/report.pdf")
        plant(temp)
        plant(shared)
        plant(keptFile)
        val ops = AndroidAppFileOps(context.noBackupFilesDir, context.cacheDir, dispatchers)

        val result = TransientFileCleaner(ops).clear()

        assertEquals(WipeStepResult.DONE, result)
        assertFalse(temp.exists())
        assertFalse(shared.exists())
        assertTrue(keptFile.exists())
        assertTrue(File(context.noBackupFilesDir, AppPaths.TEMP).isDirectory)
    }
}
