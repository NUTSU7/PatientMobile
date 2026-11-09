package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes

internal sealed interface KdfResult {
    /** The caller zeroes [key]. */
    class Derived(val key: SecretBytes) : KdfResult {
        override fun toString(): String = "Derived(***)"
    }

    data class Failed(val error: KeyError) : KdfResult
}
