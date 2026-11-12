package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

/**
 * AES-256-GCM with a non-exportable platform key, addressed by alias. The blob layout is
 * `iv (12) ‖ ciphertext ‖ tag (16)`, same as [AeadCipher]. Calls may block on secure hardware, so
 * implementations switch off the caller's dispatcher.
 */
internal interface KeystoreCipher {
    /**
     * Creates the key under [alias]. The key needs no authentication per use and is usable only while the
     * device is unlocked.
     *
     * Replaces any existing key, which makes everything wrapped by the old one unrecoverable without the
     * recovery words. Callers must refuse to call this while the alias or a vault exists, except when
     * rebuilding the key during recovery.
     */
    suspend fun generateHwKey(alias: String): KeystoreGenerateResult

    /** Encrypts under a nonce chosen by the platform. [plaintext] stays owned by the caller. */
    suspend fun seal(alias: String, plaintext: SecretBytes, aad: String): KeystoreSealResult

    /** A wrong [aad], tampered or truncated input gives `RECOVERY_REQUIRED`, never a wrong-password result. */
    suspend fun open(alias: String, ciphertext: ByteArray, aad: String): KeystoreOpenResult

    /** Null when no key exists under [alias]; `UNKNOWN` when the key exists but cannot be inspected. */
    suspend fun securityLevel(alias: String): SecurityLevel?

    /** Does nothing when [alias] does not exist. */
    suspend fun deleteAlias(alias: String)
}
