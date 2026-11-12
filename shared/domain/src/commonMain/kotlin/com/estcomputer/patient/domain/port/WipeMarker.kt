package com.estcomputer.patient.domain.port

import com.estcomputer.patient.domain.model.WipeStepResult

/** The `wipe.pending` file that tells the next start-up to finish an interrupted wipe. */
public interface WipeMarker {
    /** Writes the marker atomically. Writing it twice is fine. */
    public suspend fun write(): WipeStepResult

    /** Removes the marker. Removing a missing marker counts as done. */
    public suspend fun delete(): WipeStepResult
}
