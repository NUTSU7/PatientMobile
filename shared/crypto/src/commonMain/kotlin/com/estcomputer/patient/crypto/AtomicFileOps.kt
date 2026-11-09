package com.estcomputer.patient.crypto

/**
 * File steps behind the vault store, for the one `vault.json` and its `.tmp` sibling in a private directory.
 * Every call blocks, and reports failure as a value: callers treat `false` as "step did not happen".
 */
internal interface AtomicFileOps {
    fun read(): FileRead

    /** Writes the temp file and forces it to storage. */
    fun writeTempSynced(bytes: ByteArray): Boolean

    /** Renames the temp file over the vault file in one step. */
    fun replaceWithTemp(): Boolean

    fun syncDirectory(): Boolean

    /** True when the temp file is gone afterwards, including when it never existed. */
    fun deleteTemp(): Boolean

    /** True when the vault file is now zeros or never existed. */
    fun overwriteWithZerosSynced(): Boolean

    /** True when the vault file is gone afterwards, including when it never existed. */
    fun delete(): Boolean
}

internal sealed interface FileRead {
    data object Absent : FileRead

    class Content(val bytes: ByteArray) : FileRead {
        override fun toString(): String = "Content(***)"
    }

    data object Failed : FileRead
}
