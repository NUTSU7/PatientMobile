package com.estcomputer.patient.platform.integrity

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Warns at most once per process. Persisting "already warned" waits for the Phase 3 settings table (D8). */
public class IntegrityWarningPolicy(private val checker: DeviceIntegrityChecker) {
    private val mutex = Mutex()
    private var isWarned = false

    /** Call after each unlock. Returns the report to show the patient, or null when nothing needs a warning. */
    public suspend fun reportToShow(): IntegrityReport? =
        mutex.withLock {
            if (isWarned) return@withLock null
            val report = checker.check()
            if (report.hasConcern) isWarned = true
            report.takeIf { it.hasConcern }
        }
}
