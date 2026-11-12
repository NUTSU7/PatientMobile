package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

/**
 * AES-256-GCM with a non-exportable platform key, addressed by alias. The blob layout is
 * `iv (12) ‖ ciphertext ‖ tag (16)`, same as [AeadCipher]. Calls may block on secure hardware, so
 * implementations switch off the caller's dispatcher.
 */
internal interface KeystoreCipher {
    /**
     * Creates the key under [alias], replacing any existing one. The key needs no authentication per use and
     * is usable only while the device is unlocked.
     */
    suspend fun generateHwKey(alias: String = CryptoConstants.HW_KEY_ALIAS): SecurityLevel

    /** Encrypts under a nonce chosen by the platform. [plaintext] stays owned by the caller. */
    suspend fun seal(alias: String, plaintext: SecretBytes, aad: String): KeystoreSealResult

    /** A wrong [aad], tampered or truncated input gives `RECOVERY_REQUIRED`, never a wrong-password result. */
    suspend fun open(alias: String, ciphertext: ByteArray, aad: String): KeystoreOpenResult

    /** Null when no key exists under [alias]. */
    suspend fun securityLevel(alias: String): SecurityLevel?

    /** Does nothing when [alias] does not exist. */
    suspend fun deleteAlias(alias: String)
}
