package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

// Lightweight API only: registering BC as a JCA provider clashes with Android's built-in "BC".
internal class BouncyCastleArgon2id : Argon2id {
    override fun derive(password: SecretChars, params: KdfParams): KdfResult {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withSalt(params.salt)
            .withMemoryAsKB(params.memoryKib)
            .withIterations(params.iterations)
            .withParallelism(params.parallelism)
        val passwordBytes = password.read(Utf8Encoder::encode)
        return try {
            KdfResult.Derived(SecretBytes.wrap(compute(passwordBytes, builder, CryptoConstants.KEY_SIZE_BYTES)))
        } catch (_: OutOfMemoryError) {
            KdfResult.Failed(KeyError.OUT_OF_MEMORY)
        } finally {
            passwordBytes.fill(0)
            builder.clear()
        }
    }

    /** General form kept for the RFC 9106 vector, which also sets a secret, associated data and four lanes. */
    fun compute(password: ByteArray, builder: Argon2Parameters.Builder, length: Int): ByteArray {
        val parameters = builder.build()
        try {
            val out = ByteArray(length)
            Argon2BytesGenerator().apply { init(parameters) }.generateBytes(password, out)
            return out
        } finally {
            parameters.clear()
        }
    }
}
