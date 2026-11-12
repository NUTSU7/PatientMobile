package com.estcomputer.patient.platform.autolock

/** The patient may only choose stricter (shorter) values than the defaults. */
public data class AutoLockSettings(
    val inactivityMillis: Long = DEFAULT_INACTIVITY_MILLIS,
    val backgroundMillis: Long = DEFAULT_BACKGROUND_MILLIS,
) {
    /** Values that are not positive or are longer than the default fall back to the default. */
    public fun clamped(): AutoLockSettings =
        AutoLockSettings(
            inactivityMillis = clamp(inactivityMillis, DEFAULT_INACTIVITY_MILLIS),
            backgroundMillis = clamp(backgroundMillis, DEFAULT_BACKGROUND_MILLIS),
        )

    private fun clamp(value: Long, default: Long): Long = if (value in 1..default) value else default

    public companion object {
        public const val DEFAULT_INACTIVITY_MILLIS: Long = 5 * 60_000L
        public const val DEFAULT_BACKGROUND_MILLIS: Long = 60_000L

        /** How long the app's own camera, picker or share sheet may keep a background lock from applying. */
        public const val EXTERNAL_ACTIVITY_LIMIT_MILLIS: Long = 10 * 60_000L
    }
}
