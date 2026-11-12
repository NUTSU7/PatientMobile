package com.estcomputer.patient.crypto

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// Persisted formats are contracts: a value already written to disk never changes.
internal object CryptoConstants {
    const val HKDF_INFO_RECOVERY_WRAP: String = "patient/recovery-wrap/v1"
    const val HKDF_INFO_EXPORT_KEM: String = "patient/export-kem/v1"
    const val HKDF_INFO_DB: String = "patient/db/v1"
    const val HKDF_INFO_FILE_KEK: String = "patient/file-kek/v1"
    const val HKDF_INFO_EXPORT: String = "patient/export/v1"

    const val AAD_MK_PASSWORD: String = "patient/mk/password/v1"
    const val AAD_MK_HW: String = "patient/mk/hw/v1"
    const val AAD_MK_RECOVERY: String = "patient/mk/recovery/v1"
    const val AAD_MK_BIO: String = "patient/mk/bio/v1"
    const val AAD_BLOB_PREFIX: String = "patient/blob/"

    const val HW_KEY_ALIAS: String = "patient_hw_v1"
    const val BIO_KEY_ALIAS: String = "patient_bio_v1"

    const val VAULT_FORMAT: Int = 1

    const val KEY_SIZE_BYTES: Int = 32
    const val GCM_NONCE_BYTES: Int = 12
    const val GCM_TAG_BYTES: Int = 16
    const val GCM_TAG_BITS: Int = 128
    const val SALT_BYTES: Int = 16

    const val ARGON2_MEMORY_KIB: Int = 65_536
    const val ARGON2_ITERATIONS: Int = 3
    const val ARGON2_PARALLELISM: Int = 1
    const val ARGON2_FALLBACK_MEMORY_KIB: Int = 32_768
    const val ARGON2_FALLBACK_ITERATIONS: Int = 6
    const val CALIBRATION_TARGET_MS: Long = 500
    const val MAX_ITERATIONS: Int = 10

    val BIO_PASSWORD_MAX_AGE: Duration = 7.days

    /** How long after a password unlock a sensitive setting can change without typing the password again. */
    const val RECENT_PASSWORD_UNLOCK_MILLIS: Long = 60_000L

    const val PASSWORD_MIN_CODE_POINTS: Int = 8
    const val PASSWORD_MAX_CODE_POINTS: Int = 256

    const val WIPE_AFTER_MIN: Int = 10
    const val WIPE_AFTER_MAX: Int = 20

    private const val LOCKOUT_FROM_ATTEMPT: Int = 5
    private const val LOCKOUT_MINUTE_ATTEMPT: Int = 6
    private const val LOCKOUT_FIVE_MINUTES_ATTEMPT: Int = 7
    private const val LOCKOUT_QUARTER_HOUR_ATTEMPT: Int = 8
    private const val LOCKOUT_CAP_ATTEMPT: Int = 9

    /** Delay by failed-attempt count; counts above the last key use the last delay. */
    val LOCKOUT_SCHEDULE: Map<Int, Duration> = mapOf(
        LOCKOUT_FROM_ATTEMPT to 30.seconds,
        LOCKOUT_MINUTE_ATTEMPT to 1.minutes,
        LOCKOUT_FIVE_MINUTES_ATTEMPT to 5.minutes,
        LOCKOUT_QUARTER_HOUR_ATTEMPT to 15.minutes,
        LOCKOUT_CAP_ATTEMPT to 1.hours,
    )
}
