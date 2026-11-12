package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The only reader and writer of `vault.json`. A failed write leaves the previous vault in place. */
internal class AtomicVaultStore(
    private val fileOps: AtomicFileOps,
    private val codec: VaultCodec,
    private val dispatchers: DispatcherProvider,
) : VaultStore {
    private val mutex = Mutex()

    override suspend fun read(): VaultReadResult = locked {
        // A leftover temp file is an interrupted write; the vault file is still the last complete one.
        fileOps.deleteTemp()
        when (val file = fileOps.read()) {
            FileRead.Absent -> VaultReadResult.Absent
            FileRead.Failed -> VaultReadResult.Unreadable
            is FileRead.Content -> when (val decoded = codec.decode(file.bytes)) {
                is VaultDecodeResult.Ok -> VaultReadResult.Ok(decoded.vault)
                is VaultDecodeResult.Failed -> VaultReadResult.Corrupt
            }
        }
    }

    override suspend fun write(vault: VaultFile): VaultStoreResult = locked {
        val written = fileOps.writeTempSynced(codec.encode(vault)) &&
            fileOps.replaceWithTemp() &&
            fileOps.syncDirectory()
        if (!written) fileOps.deleteTemp()
        if (written) VaultStoreResult.DONE else VaultStoreResult.FAILED
    }

    /** Overwrites then deletes. The Keystore key is destroyed first (ARH-09), which is the real guarantee. */
    override suspend fun destroy(): VaultStoreResult = locked {
        val tempGone = fileOps.deleteTemp()
        val zeroed = fileOps.overwriteWithZerosSynced()
        val deleted = fileOps.delete()
        if (tempGone && zeroed && deleted) VaultStoreResult.DONE else VaultStoreResult.FAILED
    }

    private suspend fun <T> locked(block: () -> T): T = mutex.withLock { withContext(dispatchers.io) { block() } }
}
