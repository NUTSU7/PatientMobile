package com.estcomputer.patient.crypto

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

/** [directory] must be private app storage excluded from backup, e.g. `noBackupFilesDir`. */
internal class AndroidAtomicFileOps(private val directory: File) : AtomicFileOps {
    private val vaultFile = File(directory, VAULT_FILE_NAME)
    private val tempFile = File(directory, TEMP_FILE_NAME)

    override fun read(): FileRead =
        try {
            if (vaultFile.exists()) FileRead.Content(vaultFile.readBytes()) else FileRead.Absent
        } catch (_: IOException) {
            FileRead.Failed
        }

    override fun writeTempSynced(bytes: ByteArray): Boolean =
        attempt {
            FileOutputStream(tempFile).use {
                it.write(bytes)
                it.fd.sync()
            }
        }

    override fun replaceWithTemp(): Boolean = tempFile.renameTo(vaultFile)

    // Some filesystems refuse to open a directory; the rename is still atomic, only its durability is unproven.
    override fun syncDirectory(): Boolean {
        attempt { FileChannel.open(directory.toPath(), StandardOpenOption.READ).use { it.force(true) } }
        return true
    }

    override fun deleteTemp(): Boolean = !tempFile.exists() || tempFile.delete()

    override fun overwriteWithZerosSynced(): Boolean =
        !vaultFile.exists() || attempt {
            RandomAccessFile(vaultFile, "rw").use {
                it.write(ByteArray(it.length().toInt()))
                it.fd.sync()
            }
        }

    override fun delete(): Boolean = !vaultFile.exists() || vaultFile.delete()

    private inline fun attempt(block: () -> Unit): Boolean =
        try {
            block()
            true
        } catch (_: IOException) {
            false
        }

    companion object {
        const val VAULT_FILE_NAME = "vault.json"
        const val TEMP_FILE_NAME = "vault.json.tmp"
    }
}
