package com.estcomputer.patient.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CryptoConstantsTest {

    @Test
    fun constants_neverChange() {
        with(CryptoConstants) {
            assertEquals("patient/recovery-wrap/v1", HKDF_INFO_RECOVERY_WRAP)
            assertEquals("patient/export-kem/v1", HKDF_INFO_EXPORT_KEM)
            assertEquals("patient/db/v1", HKDF_INFO_DB)
            assertEquals("patient/file-kek/v1", HKDF_INFO_FILE_KEK)
            assertEquals("patient/export/v1", HKDF_INFO_EXPORT)

            assertEquals("patient/mk/password/v1", AAD_MK_PASSWORD)
            assertEquals("patient/mk/hw/v1", AAD_MK_HW)
            assertEquals("patient/mk/recovery/v1", AAD_MK_RECOVERY)
            assertEquals("patient/mk/bio/v1", AAD_MK_BIO)
            assertEquals("patient/blob/", AAD_BLOB_PREFIX)

            assertEquals("patient_hw_v1", HW_KEY_ALIAS)
            assertEquals("patient_bio_v1", BIO_KEY_ALIAS)

            assertEquals(1, VAULT_FORMAT)

            assertEquals(32, KEY_SIZE_BYTES)
            assertEquals(12, GCM_NONCE_BYTES)
            assertEquals(16, GCM_TAG_BYTES)
            assertEquals(128, GCM_TAG_BITS)
            assertEquals(16, SALT_BYTES)

            assertEquals(65_536, ARGON2_MEMORY_KIB)
            assertEquals(3, ARGON2_ITERATIONS)
            assertEquals(1, ARGON2_PARALLELISM)
            assertEquals(32_768, ARGON2_FALLBACK_MEMORY_KIB)
            assertEquals(6, ARGON2_FALLBACK_ITERATIONS)
            assertEquals(500L, CALIBRATION_TARGET_MS)
            assertEquals(10, MAX_ITERATIONS)

            assertEquals(7.days, BIO_PASSWORD_MAX_AGE)
            assertEquals(60_000L, RECENT_PASSWORD_UNLOCK_MILLIS)

            assertEquals(8, PASSWORD_MIN_CODE_POINTS)
            assertEquals(256, PASSWORD_MAX_CODE_POINTS)

            assertEquals(10, WIPE_AFTER_MIN)
            assertEquals(20, WIPE_AFTER_MAX)

            assertEquals(
                mapOf(5 to 30.seconds, 6 to 1.minutes, 7 to 5.minutes, 8 to 15.minutes, 9 to 1.hours),
                LOCKOUT_SCHEDULE,
            )
        }
    }

    @Test
    fun gcmSizes_areConsistent() {
        assertEquals(CryptoConstants.GCM_TAG_BYTES * Byte.SIZE_BITS, CryptoConstants.GCM_TAG_BITS)
    }
}
