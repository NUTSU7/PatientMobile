package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

internal sealed interface OpenResult {
    /** The caller zeroes [plaintext]. */
    class Ok(val plaintext: SecretBytes) : OpenResult {
        override fun toString(): String = "Ok(***)"
    }

    data object AuthFailed : OpenResult
}
