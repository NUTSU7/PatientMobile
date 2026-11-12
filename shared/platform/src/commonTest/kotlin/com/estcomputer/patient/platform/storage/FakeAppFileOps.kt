package com.estcomputer.patient.platform.storage

/** An in-memory file system keyed by directory and relative path. A child path is `parent/child`. */
class FakeAppFileOps(vararg initial: Pair<AppDirectory, String>) : AppFileOps {
    val files: MutableSet<Pair<AppDirectory, String>> = initial.toMutableSet()
    val locked: MutableSet<Pair<AppDirectory, String>> = mutableSetOf()
    val calls = mutableListOf<String>()

    override suspend fun deleteRecursively(directory: AppDirectory, relativePath: String): Boolean {
        calls += "delete:$relativePath"
        return remove(directory, relativePath, includeRoot = true)
    }

    override suspend fun clearDirectory(directory: AppDirectory, relativePath: String): Boolean {
        calls += "clear:$relativePath"
        return remove(directory, relativePath, includeRoot = false)
    }

    private fun remove(directory: AppDirectory, relativePath: String, includeRoot: Boolean): Boolean {
        val doomed = files.filter { (dir, path) ->
            dir == directory && (path.startsWith("$relativePath/") || (includeRoot && path == relativePath))
        }
        val blocked = doomed.any { it in locked }
        if (!blocked) files.removeAll(doomed.toSet())
        return !blocked
    }
}
