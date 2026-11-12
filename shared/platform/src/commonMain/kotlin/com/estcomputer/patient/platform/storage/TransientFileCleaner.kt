package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.model.WipeStepResult
import com.estcomputer.patient.domain.port.TransientFiles

/** Empties `tmp/` and the share cache. Both are tried even when the first one fails. */
public class TransientFileCleaner(private val fileOps: AppFileOps) : TransientFiles {

    override suspend fun clear(): WipeStepResult {
        val temp = fileOps.clearDirectory(AppDirectory.NO_BACKUP_FILES, AppPaths.TEMP)
        val share = fileOps.clearDirectory(AppDirectory.CACHE, AppPaths.SHARE)
        return if (temp && share) WipeStepResult.DONE else WipeStepResult.FAILED
    }
}
