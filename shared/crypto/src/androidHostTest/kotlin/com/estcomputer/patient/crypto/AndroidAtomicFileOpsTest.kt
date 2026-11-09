package com.estcomputer.patient.crypto

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AndroidAtomicFileOpsTest {
    private val directory: File = Files.createTempDirectory("vault-ops").toFile()
    private val ops = AndroidAtomicFileOps(directory)
    private val vaultFile = File(directory, AndroidAtomicFileOps.VAULT_FILE_NAME)
    private val tempFile = File(directory, AndroidAtomicFileOps.TEMP_FILE_NAME)

    @AfterTest
    fun cleanUp() {
        directory.deleteRecursively()
    }

    private fun writeAndReplace(bytes: ByteArray) {
        assertTrue(ops.writeTempSynced(bytes))
        assertTrue(ops.replaceWithTemp())
    }

    @Test
    fun read_noFile_absent() {
        assertEquals(FileRead.Absent, ops.read())
    }

    @Test
    fun writeTempSynced_thenReplace_vaultHasBytesAndTempIsGone() {
        writeAndReplace(byteArrayOf(1, 2, 3))

        assertContentEquals(byteArrayOf(1, 2, 3), assertIs<FileRead.Content>(ops.read()).bytes)
        assertFalse(tempFile.exists())
    }

    @Test
    fun replaceWithTemp_existingVault_replacedWholly() {
        writeAndReplace(ByteArray(LONG_CONTENT) { 1 })
        writeAndReplace(byteArrayOf(9))

        assertContentEquals(byteArrayOf(9), vaultFile.readBytes())
    }

    @Test
    fun writeTempSynced_interrupted_leavesOldVaultIntact() {
        writeAndReplace(byteArrayOf(1, 2, 3))

        assertTrue(ops.writeTempSynced(byteArrayOf(7, 7, 7, 7)))

        assertContentEquals(byteArrayOf(1, 2, 3), vaultFile.readBytes())
        assertTrue(tempFile.exists())
    }

    @Test
    fun deleteTemp_leftoverTemp_removed() {
        tempFile.writeBytes(byteArrayOf(5))

        assertTrue(ops.deleteTemp())

        assertFalse(tempFile.exists())
        assertTrue(ops.deleteTemp())
    }

    @Test
    fun overwriteWithZerosSynced_keepsLengthAndZeroesContent() {
        writeAndReplace(ByteArray(LONG_CONTENT) { 0x5A })

        assertTrue(ops.overwriteWithZerosSynced())

        assertContentEquals(ByteArray(LONG_CONTENT), vaultFile.readBytes())
    }

    @Test
    fun delete_existingVault_removesFile() {
        writeAndReplace(byteArrayOf(1))

        assertTrue(ops.delete())

        assertFalse(vaultFile.exists())
        assertEquals(FileRead.Absent, ops.read())
    }

    @Test
    fun overwriteAndDelete_noFile_succeed() {
        assertTrue(ops.overwriteWithZerosSynced())
        assertTrue(ops.delete())
    }

    @Test
    fun writeTempSynced_missingDirectory_returnsFalse() {
        val missing = AndroidAtomicFileOps(File(directory, "missing"))

        assertFalse(missing.writeTempSynced(byteArrayOf(1)))
    }

    private companion object {
        const val LONG_CONTENT = 4096
    }
}
