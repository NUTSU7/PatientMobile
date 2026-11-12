package com.estcomputer.patient.platform.storage

import com.estcomputer.patient.domain.session.SessionCloser

/** The doctor PDF the patient chose to share is the only plaintext the app writes; it must not outlive the session. */
public class ShareCacheCleaner(private val fileOps: AppFileOps) : SessionCloser {
    override suspend fun close() {
        fileOps.clearDirectory(AppDirectory.CACHE, AppPaths.SHARE)
    }
}
