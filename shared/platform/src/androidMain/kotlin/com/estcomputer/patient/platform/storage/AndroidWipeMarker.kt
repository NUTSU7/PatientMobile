package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.WipeStepResult
import com.estcomputer.patient.domain.port.WipeMarker
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

public class AndroidWipeMarker(
    noBackupFilesDir: File,
    private val dispatchers: DispatcherProvider,
) : WipeMarker {
    private val marker = File(noBackupFilesDir, AppPaths.WIPE_MARKER)
    private val staging = File(noBackupFilesDir, "${AppPaths.WIPE_MARKER}.tmp")

    override suspend fun write(): WipeStepResult =
        withContext(dispatchers.io) {
            try {
                FileOutputStream(staging).use { stream ->
                    stream.write(MARKER_BYTE)
                    stream.fd.sync()
                }
                // The marker either exists whole or not at all: it is renamed into place after the sync.
                if (staging.renameTo(marker)) WipeStepResult.DONE else WipeStepResult.FAILED
            } catch (_: IOException) {
                WipeStepResult.FAILED
            }
        }

    override suspend fun delete(): WipeStepResult =
        withContext(dispatchers.io) {
            staging.delete()
            if (marker.delete() || !marker.exists()) WipeStepResult.DONE else WipeStepResult.FAILED
        }

    private companion object {
        const val MARKER_BYTE = 1
    }
}
