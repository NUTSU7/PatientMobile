package com.estcomputer.patient.crypto

internal sealed interface VaultReadResult {
    data object Absent : VaultReadResult

    class Ok(val vault: VaultFile) : VaultReadResult {
        override fun toString(): String = "Ok($vault)"
    }

    /** The file exists but is not a valid vault. */
    data object Corrupt : VaultReadResult

    /** The file could not be read, so nothing is known about it. */
    data object Unreadable : VaultReadResult
}

internal enum class VaultStoreResult { DONE, FAILED }
