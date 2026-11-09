package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.google.crypto.tink.subtle.X25519 as TinkX25519Function

internal class TinkX25519 : X25519 {
    override fun publicKey(privateKey: SecretBytes): ByteArray =
        privateKey.read { TinkX25519Function.publicFromPrivate(it) }
}
