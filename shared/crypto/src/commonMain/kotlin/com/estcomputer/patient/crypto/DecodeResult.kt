package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

internal sealed interface DecodeResult {
    class Ok(val entropy: SecretBytes) : DecodeResult {
        override fun toString(): String = "Ok(***)"
    }

    data class UnknownWord(val index: Int) : DecodeResult

    data object WrongWordCount : DecodeResult

    data object ChecksumMismatch : DecodeResult
}
