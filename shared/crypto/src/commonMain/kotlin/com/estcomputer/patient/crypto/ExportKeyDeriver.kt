package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

internal class ExportKeyDeriver(private val keyDerivation: KeyDerivation, private val x25519: X25519) {
    /** The 32-byte X25519 public key stored in `vault.json`. The private key never leaves this call. */
    fun publicKey(recoverySecret: SecretBytes): ByteArray {
        val privateKey = keyDerivation.deriveExportPrivate(recoverySecret)
        try {
            return x25519.publicKey(privateKey)
        } finally {
            privateKey.zero()
        }
    }
}
