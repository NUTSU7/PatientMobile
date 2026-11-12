package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File

/** Roots are `Context.noBackupFilesDir` and `Context.cacheDir`, passed in so tests can use temp folders. */
public class AndroidAppFileOps(
    private val noBackupFilesDir: File,
    private val cacheDir: File,
    private val dispatchers: DispatcherProvider,
) : AppFileOps {

    override suspend fun deleteRecursively(directory: AppDirectory, relativePath: String): Boolean =
        withContext(dispatchers.io) {
            val target = File(rootOf(directory), relativePath)
            target.deleteRecursively() || !target.exists()
        }

    override suspend fun clearDirectory(directory: AppDirectory, relativePath: String): Boolean =
        withContext(dispatchers.io) {
            val target = File(rootOf(directory), relativePath)
            val children = target.listFiles() ?: return@withContext !target.exists()
            children.map { it.deleteRecursively() || !it.exists() }.all { it }
        }

    override suspend fun exists(directory: AppDirectory, relativePath: String): Boolean =
        withContext(dispatchers.io) { File(rootOf(directory), relativePath).exists() }

    private fun rootOf(directory: AppDirectory): File =
        when (directory) {
            AppDirectory.NO_BACKUP_FILES -> noBackupFilesDir
            AppDirectory.CACHE -> cacheDir
        }
}
