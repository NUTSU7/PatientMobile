package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

/** AES-256-GCM with in-memory keys. Wire format: `nonce (12) ‖ ciphertext ‖ tag (16)`. */
internal interface AeadCipher {
    /** Encrypts under a fresh random nonce. [key] and [plaintext] stay owned by the caller. */
    fun seal(key: SecretBytes, plaintext: SecretBytes, aad: String): ByteArray

    /** Returns `AuthFailed` for a wrong key, wrong [aad], tampered or truncated input. */
    fun open(key: SecretBytes, ciphertext: ByteArray, aad: String): OpenResult
}
