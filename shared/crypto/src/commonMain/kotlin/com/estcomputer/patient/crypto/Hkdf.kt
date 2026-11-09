package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

internal interface Hkdf {
    /** HKDF-SHA256 with an empty salt and a 32-byte output. The caller zeroes the result. */
    fun derive(ikm: SecretBytes, info: String): SecretBytes
}
