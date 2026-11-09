package com.estcomputer.patient.crypto

/** Argon2id parameters as stored in `vault.json`. The salt is not secret. */
internal class KdfParams(
    val salt: ByteArray,
    val memoryKib: Int,
    val iterations: Int,
    val parallelism: Int,
) {
    // Stored params outside what calibration can produce mean an edited vault, e.g. a downgrade.
    val isWithinBounds: Boolean
        get() = salt.size == CryptoConstants.SALT_BYTES &&
            memoryKib >= CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB &&
            iterations in 1..CryptoConstants.MAX_ITERATIONS &&
            parallelism == CryptoConstants.ARGON2_PARALLELISM
}
