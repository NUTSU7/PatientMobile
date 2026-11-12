package com.estcomputer.patient.domain.port

import com.estcomputer.patient.domain.model.WipeStepResult

public fun interface StorageWiper {
    /** Deletes the database files, blobs, temp files, alarm file and share cache. Missing files are fine. */
    public suspend fun deleteAll(): WipeStepResult
}
