package com.estcomputer.patient.platform.storage

/** The app's storage roots. Android maps them to `noBackupFilesDir` and `cacheDir`. */
public enum class AppDirectory {
    NO_BACKUP_FILES,
    CACHE,
}

/** One place that is deleted by a full wipe. */
public enum class WipeTarget(public val directory: AppDirectory, public val relativePath: String) {
    DATABASE(AppDirectory.NO_BACKUP_FILES, AppPaths.DATABASE),
    DATABASE_WAL(AppDirectory.NO_BACKUP_FILES, AppPaths.DATABASE_WAL),
    DATABASE_SHM(AppDirectory.NO_BACKUP_FILES, AppPaths.DATABASE_SHM),
    DATABASE_JOURNAL(AppDirectory.NO_BACKUP_FILES, AppPaths.DATABASE_JOURNAL),
    BLOBS(AppDirectory.NO_BACKUP_FILES, AppPaths.BLOBS),
    TEMP(AppDirectory.NO_BACKUP_FILES, AppPaths.TEMP),
    ALARMS(AppDirectory.NO_BACKUP_FILES, AppPaths.ALARMS),
    SHARE_CACHE(AppDirectory.CACHE, AppPaths.SHARE),
}

/** Relative names of everything the app writes. The vault file and the wipe marker are handled apart. */
public object AppPaths {
    public const val DATABASE: String = "patient.db"
    public const val DATABASE_WAL: String = "patient.db-wal"
    public const val DATABASE_SHM: String = "patient.db-shm"
    public const val DATABASE_JOURNAL: String = "patient.db-journal"
    public const val BLOBS: String = "blobs"
    public const val TEMP: String = "tmp"
    public const val ALARMS: String = "alarms.json"
    public const val SHARE: String = "share"
    public const val WIPE_MARKER: String = "wipe.pending"
}
