package com.estcomputer.patient.platform.storage

/** Answers the two questions the key manager asks at launch, from the same paths the wipe deletes. */
public class AppDataInspector(private val fileOps: AppFileOps) {

    /** True when an earlier wipe did not finish. */
    public suspend fun hasWipeMarker(): Boolean = fileOps.exists(AppDirectory.NO_BACKUP_FILES, AppPaths.WIPE_MARKER)

    /** True when the database, blobs or alarm file exist. They must never exist without a vault. */
    public suspend fun hasAppData(): Boolean =
        DATA_PATHS.any { fileOps.exists(AppDirectory.NO_BACKUP_FILES, it) }

    private companion object {
        val DATA_PATHS = listOf(AppPaths.DATABASE, AppPaths.BLOBS, AppPaths.ALARMS)
    }
}
