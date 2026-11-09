package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError

internal sealed interface VaultDecodeResult {
    class Ok(val vault: VaultFile) : VaultDecodeResult {
        override fun toString(): String = "Ok(${vault})"
    }

    data class Failed(val error: KeyError) : VaultDecodeResult
}
