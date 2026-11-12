package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

/** Seals a plain byte array; the temporary secret copy is zeroed. */
internal suspend fun KeystoreCipher.sealBytes(alias: String, bytes: ByteArray, aad: String): KeystoreSealResult {
    val secret = SecretBytes.copyOf(bytes)
    try {
        return seal(alias, secret, aad)
    } finally {
        secret.zero()
    }
}
