package com.estcomputer.patient.crypto

internal interface DeviceSecurity {
    /** True when the device has a secure lock screen. */
    fun isDeviceSecure(): Boolean
}

internal interface SecureRandomSource {
    /** Cryptographically secure random bytes. The caller owns and zeroes the array. */
    fun nextBytes(size: Int): ByteArray
}

internal interface AppDataProbe {
    /** True when the interrupted-wipe marker file exists. */
    suspend fun hasWipeMarker(): Boolean

    /** True when the database, blobs or alarms file exist. */
    suspend fun hasAppData(): Boolean
}

/**
 * Asks for a full wipe. Implemented outside this module, because the wipe also destroys the keys.
 * The implementation writes the `wipe.pending` marker before deleting anything and must be idempotent, so
 * a retry after a crash converges.
 */
internal fun interface WipeRequester {
    suspend fun wipeAllData()
}

internal interface VaultStore {
    suspend fun read(): VaultReadResult

    suspend fun write(vault: VaultFile): VaultStoreResult

    suspend fun destroy(): VaultStoreResult
}
