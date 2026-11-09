package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.google.crypto.tink.subtle.Hkdf as TinkHkdf

internal class HkdfSha256 : Hkdf {
    override fun derive(ikm: SecretBytes, info: String): SecretBytes {
        val okm = ikm.read { compute(it, ByteArray(0), info.encodeToByteArray(), CryptoConstants.KEY_SIZE_BYTES) }
        return SecretBytes.wrap(okm)
    }

    /** General form kept for the RFC 5869 vectors; an empty salt means HashLen zero bytes. */
    fun compute(ikm: ByteArray, salt: ByteArray, info: ByteArray, length: Int): ByteArray =
        TinkHkdf.computeHkdf(MAC_ALGORITHM, ikm, salt, info, length)

    private companion object {
        const val MAC_ALGORITHM = "HMACSHA256"
    }
}
