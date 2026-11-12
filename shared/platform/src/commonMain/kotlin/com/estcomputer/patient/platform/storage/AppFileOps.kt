package com.estcomputer.patient.platform.storage

public interface AppFileOps {
    /** Deletes a file or a directory tree. True when nothing is left at the path, even if it never existed. */
    public suspend fun deleteRecursively(directory: AppDirectory, relativePath: String): Boolean

    /** Deletes everything inside the directory but keeps the directory itself. Returns true when it is empty. */
    public suspend fun clearDirectory(directory: AppDirectory, relativePath: String): Boolean

    /** True when a file or directory exists at the path. */
    public suspend fun exists(directory: AppDirectory, relativePath: String): Boolean
}
