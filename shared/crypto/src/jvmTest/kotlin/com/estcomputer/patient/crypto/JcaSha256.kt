package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.Sha256
import java.security.MessageDigest

internal class JcaSha256 : Sha256 {
    override fun digest(input: ByteArray): ByteArray = MessageDigest.getInstance(ALGORITHM).digest(input)

    private companion object {
        const val ALGORITHM = "SHA-256"
    }
}
