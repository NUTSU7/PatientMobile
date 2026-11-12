package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.model.WipeStepResult
import com.estcomputer.patient.domain.port.StorageWiper

/** Which targets could not be deleted. Holds names of places, never file contents. */
public class StorageWipeReport(public val failed: Set<WipeTarget>) {
    public val isComplete: Boolean get() = failed.isEmpty()
}

public class AppStorageWiper(private val fileOps: AppFileOps) : StorageWiper {

    /** Tries every target even after one fails, so a single locked file never leaves the rest behind. */
    public suspend fun deleteTargets(): StorageWipeReport {
        val failed = WipeTarget.entries.filterNot { fileOps.deleteRecursively(it.directory, it.relativePath) }
        return StorageWipeReport(failed.toSet())
    }

    override suspend fun deleteAll(): WipeStepResult =
        if (deleteTargets().isComplete) WipeStepResult.DONE else WipeStepResult.FAILED
}
