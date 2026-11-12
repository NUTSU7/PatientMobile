package com.estcomputer.patient.domain.port

import com.estcomputer.patient.domain.model.WipeStepResult

/** Plaintext or half-written files that must not survive a process restart: temp files and the share cache. */
public fun interface TransientFiles {
    /** Empties the temp directory and the share cache. Missing directories are fine. */
    public suspend fun clear(): WipeStepResult
}
