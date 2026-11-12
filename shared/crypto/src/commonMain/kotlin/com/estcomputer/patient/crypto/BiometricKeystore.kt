package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.port.BiometricChallenge

/**
 * AES-256-GCM with a platform key that needs the patient's biometric for every use. Blob layout as
 * [KeystoreCipher]. The key is used through a challenge: begin, let the patient authenticate, then complete.
 */
internal interface BiometricKeystore {
    /**
     * Creates the key under [alias], invalidated when biometrics are enrolled or removed. Replaces any
     * existing key.
     */
    suspend fun generateBioKey(alias: String): BiometricKeystoreGenerateResult

    /** Challenge for sealing a new blob. Used for enrolment and refresh. */
    suspend fun beginSeal(alias: String): BiometricKeystoreBeginResult

    /** Challenge for opening [blob], bound to the nonce stored in it. */
    suspend fun beginOpen(alias: String, blob: ByteArray): BiometricKeystoreBeginResult

    /**
     * Call only after the patient authenticated for [challenge]. A challenge is single use, and [plaintext]
     * stays owned by the caller.
     */
    suspend fun seal(challenge: BiometricChallenge, plaintext: SecretBytes, aad: String): BiometricKeystoreSealResult

    /** Call only after the patient authenticated for [challenge]. A challenge is single use. */
    suspend fun open(challenge: BiometricChallenge, blob: ByteArray, aad: String): BiometricKeystoreOpenResult

    /** Does nothing when [alias] does not exist. */
    suspend fun deleteAlias(alias: String)
}
