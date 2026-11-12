package com.estcomputer.patient.platform.integrity

/** Everything the rules read from the device, so tests can supply any combination. */
internal interface IntegritySources {
    val buildTags: String?
    val buildType: String?
    val isAppDebuggable: Boolean

    fun fileExists(path: String): Boolean

    fun isDebuggerConnected(): Boolean
}

// File checks only: no process execution, no hidden APIs, and no package queries (they need a manifest entry).
internal object IntegrityRules {
    val SU_PATHS: List<String> = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/su",
        "/vendor/bin/su",
        "/su/bin/su",
        "/data/local/su",
        "/data/local/bin/su",
        "/data/local/xbin/su",
    )

    val MAGISK_PATHS: List<String> = listOf(
        "/sbin/.magisk",
        "/data/adb/magisk",
        "/data/adb/magisk.db",
        "/cache/magisk.log",
    )

    private const val TEST_KEYS_TAG = "test-keys"
    private val DEVELOPMENT_BUILD_TYPES = setOf("eng", "userdebug")

    fun evaluate(sources: IntegritySources): IntegrityReport =
        IntegrityReport(
            isRootSuspected = isRootSuspected(sources),
            isDebuggable = sources.isAppDebuggable,
            isDebuggerAttached = sources.isDebuggerConnected(),
        )

    private fun isRootSuspected(sources: IntegritySources): Boolean =
        (SU_PATHS + MAGISK_PATHS).any(sources::fileExists) ||
            sources.buildTags?.contains(TEST_KEYS_TAG) == true ||
            sources.buildType in DEVELOPMENT_BUILD_TYPES
}
