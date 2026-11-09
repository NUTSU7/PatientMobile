package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretChars

internal interface Argon2id {
    /**
     * Blocking 32-byte Argon2id over the UTF-8 bytes of [password], which stays owned by the caller.
     * Returns `Failed(OUT_OF_MEMORY)` when the memory can't be allocated.
     */
    fun derive(password: SecretChars, params: KdfParams): KdfResult
}
