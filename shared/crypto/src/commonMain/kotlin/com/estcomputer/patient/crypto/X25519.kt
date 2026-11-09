package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

internal interface X25519 {
    /** Public key for a 32-byte private scalar, clamped per RFC 7748. [privateKey] stays owned by the caller. */
    fun publicKey(privateKey: SecretBytes): ByteArray
}
