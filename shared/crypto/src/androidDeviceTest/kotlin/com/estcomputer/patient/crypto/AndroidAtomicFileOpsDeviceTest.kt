package com.estcomputer.patient.crypto

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AndroidAtomicFileOpsDeviceTest {
    private lateinit var directory: File
    private lateinit var ops: AndroidAtomicFileOps

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        directory = File(context.noBackupFilesDir, TEST_DIRECTORY).also { it.mkdirs() }
        ops = AndroidAtomicFileOps(directory)
    }

    @After
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun crashBetweenTempWriteAndRename_leavesOldVaultIntact() {
        assertTrue(ops.writeTempSynced(byteArrayOf(1, 2, 3)))
        assertTrue(ops.replaceWithTemp())

        assertTrue(ops.writeTempSynced(byteArrayOf(9, 9)))

        assertContentEquals(byteArrayOf(1, 2, 3), assertIs<FileRead.Content>(ops.read()).bytes)
    }

    @Test
    fun replaceWithTemp_overExistingVault_replacesWholly() {
        assertTrue(ops.writeTempSynced(ByteArray(LONG_CONTENT) { 1 }))
        assertTrue(ops.replaceWithTemp())
        assertTrue(ops.writeTempSynced(byteArrayOf(9)))
        assertTrue(ops.replaceWithTemp())
        assertTrue(ops.syncDirectory())

        assertContentEquals(byteArrayOf(9), assertIs<FileRead.Content>(ops.read()).bytes)
        assertFalse(File(directory, AndroidAtomicFileOps.TEMP_FILE_NAME).exists())
    }

    @Test
    fun destroy_zeroesThenRemovesVaultFile() {
        assertTrue(ops.writeTempSynced(ByteArray(LONG_CONTENT) { 0x5A }))
        assertTrue(ops.replaceWithTemp())

        assertTrue(ops.overwriteWithZerosSynced())
        val vaultFile = File(directory, AndroidAtomicFileOps.VAULT_FILE_NAME)
        assertContentEquals(ByteArray(LONG_CONTENT), vaultFile.readBytes())

        assertTrue(ops.delete())
        assertEquals(FileRead.Absent, ops.read())
    }

    private companion object {
        const val TEST_DIRECTORY = "patient_test_vault_ops"
        const val LONG_CONTENT = 4096
    }
}
