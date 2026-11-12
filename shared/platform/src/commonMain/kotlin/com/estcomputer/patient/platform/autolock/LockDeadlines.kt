package com.estcomputer.patient.platform.autolock

/** The timing state behind [AutoLockPolicy]: pure arithmetic on monotonic milliseconds. */
internal class LockDeadlines(private val settings: AutoLockSettings) {
    var lastInteractionAt: Long = 0
    var backgroundedAt: Long? = null
    var externalActivitySetAt: Long? = null

    val isBackgrounded: Boolean get() = backgroundedAt != null

    fun beginForegroundSession(now: Long) {
        backgroundedAt = null
        externalActivitySetAt = null
        lastInteractionAt = now
    }

    fun millisUntilLock(now: Long): Long {
        val away = backgroundedAt
        val deadline = if (away == null) {
            lastInteractionAt + settings.inactivityMillis
        } else {
            val backgroundDeadline = away + settings.backgroundMillis
            val externalSetAt = externalActivitySetAt
            if (externalSetAt == null) {
                backgroundDeadline
            } else {
                maxOf(backgroundDeadline, externalSetAt + AutoLockSettings.EXTERNAL_ACTIVITY_LIMIT_MILLIS)
            }
        }
        return deadline - now
    }
}
