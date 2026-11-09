package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AtomicVaultStoreTest {
    private class SingleDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val default: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    private class FakeAtomicFileOps : AtomicFileOps {
        var vaultBytes: ByteArray? = null
        var tempBytes: ByteArray? = null
        var failingStep: String? = null
        val calls = mutableListOf<String>()

        private fun step(name: String, action: () -> Unit): Boolean {
            calls.add(name)
            if (failingStep == name) return false
            action()
            return true
        }

        override fun read(): FileRead {
            calls.add(READ)
            if (failingStep == READ) return FileRead.Failed
            return vaultBytes?.let { FileRead.Content(it.copyOf()) } ?: FileRead.Absent
        }

        override fun writeTempSynced(bytes: ByteArray) = step(WRITE_TEMP) { tempBytes = bytes.copyOf() }

        override fun replaceWithTemp() = step(REPLACE) {
            vaultBytes = tempBytes
            tempBytes = null
        }

        override fun syncDirectory() = step(SYNC_DIRECTORY) {}

        override fun deleteTemp() = step(DELETE_TEMP) { tempBytes = null }

        override fun overwriteWithZerosSynced() =
            step(OVERWRITE) { vaultBytes = vaultBytes?.let { ByteArray(it.size) } }

        override fun delete() = step(DELETE) { vaultBytes = null }
    }

    private val ops = FakeAtomicFileOps()
    private val codec = VaultCodec()

    private fun TestScope.store() =
        AtomicVaultStore(ops, codec, SingleDispatcherProvider(StandardTestDispatcher(testScheduler)))

    private fun vault(failedAttempts: Int = 0) = VaultFile(
        format = CryptoConstants.VAULT_FORMAT,
        createdAt = "2026-11-06T10:00:00Z",
        kdf = VaultFile.Kdf(
            alg = "argon2id",
            salt = ByteArray(CryptoConstants.SALT_BYTES),
            memoryKib = CryptoConstants.ARGON2_MEMORY_KIB,
            iterations = CryptoConstants.ARGON2_ITERATIONS,
            parallelism = CryptoConstants.ARGON2_PARALLELISM,
        ),
        hwKeyAlias = CryptoConstants.HW_KEY_ALIAS,
        wrappedMkPassword = ByteArray(WRAPPED_PASSWORD_BYTES),
        wrappedMkRecovery = ByteArray(WRAPPED_RECOVERY_BYTES),
        wrappedMkBiometric = null,
        bioKeyAlias = null,
        exportPublicKey = ByteArray(CryptoConstants.KEY_SIZE_BYTES),
        failedAttempts = failedAttempts,
        wipeAfterFailedAttempts = 0,
        lockoutUntilElapsed = 0,
        lockoutBootCount = 0,
    )

    private suspend fun AtomicVaultStore.readAttempts(): Int =
        assertIs<VaultReadResult.Ok>(read()).vault.failedAttempts

    @Test
    fun write_success_runsStepsInOrder() = runTest {
        assertEquals(VaultStoreResult.DONE, store().write(vault()))

        assertEquals(listOf(WRITE_TEMP, REPLACE, SYNC_DIRECTORY), ops.calls)
    }

    @Test
    fun write_thenRead_returnsSameVault() = runTest {
        val store = store()
        store.write(vault(failedAttempts = 4))

        assertEquals(4, store.readAttempts())
    }

    @Test
    fun write_tempWriteFails_oldVaultReadableAndTempRemoved() = runTest {
        val store = store()
        store.write(vault(failedAttempts = 1))
        ops.failingStep = WRITE_TEMP
        ops.calls.clear()

        assertEquals(VaultStoreResult.FAILED, store.write(vault(failedAttempts = 2)))

        assertEquals(listOf(WRITE_TEMP, DELETE_TEMP), ops.calls)
        ops.failingStep = null
        assertEquals(1, store.readAttempts())
        assertNull(ops.tempBytes)
    }

    @Test
    fun write_replaceFails_oldVaultReadableAndTempRemoved() = runTest {
        val store = store()
        store.write(vault(failedAttempts = 1))
        ops.failingStep = REPLACE
        ops.calls.clear()

        assertEquals(VaultStoreResult.FAILED, store.write(vault(failedAttempts = 2)))

        assertEquals(listOf(WRITE_TEMP, REPLACE, DELETE_TEMP), ops.calls)
        ops.failingStep = null
        assertEquals(1, store.readAttempts())
        assertNull(ops.tempBytes)
    }

    @Test
    fun write_directorySyncFails_reportsFailed() = runTest {
        ops.failingStep = SYNC_DIRECTORY

        assertEquals(VaultStoreResult.FAILED, store().write(vault()))
    }

    @Test
    fun write_afterFailedWrite_succeeds() = runTest {
        val store = store()
        ops.failingStep = WRITE_TEMP
        store.write(vault(failedAttempts = 1))
        ops.failingStep = null

        assertEquals(VaultStoreResult.DONE, store.write(vault(failedAttempts = 2)))
        assertEquals(2, store.readAttempts())
    }

    @Test
    fun read_leftoverTemp_deletedAndVaultFileWins() = runTest {
        val store = store()
        store.write(vault(failedAttempts = 1))
        ops.tempBytes = "half written".encodeToByteArray()
        ops.calls.clear()

        assertEquals(1, store.readAttempts())

        assertEquals(listOf(DELETE_TEMP, READ), ops.calls)
        assertNull(ops.tempBytes)
    }

    @Test
    fun read_noFile_absent() = runTest {
        assertEquals(VaultReadResult.Absent, store().read())
    }

    @Test
    fun read_invalidContent_corrupt() = runTest {
        ops.vaultBytes = "not a vault".encodeToByteArray()

        assertEquals(VaultReadResult.Corrupt, store().read())
    }

    @Test
    fun read_ioFailure_unreadableNotAbsent() = runTest {
        ops.failingStep = READ

        assertEquals(VaultReadResult.Unreadable, store().read())
    }

    @Test
    fun destroy_overwritesBeforeDeleting() = runTest {
        val store = store()
        store.write(vault())
        ops.calls.clear()
        val sizeBefore = ops.vaultBytes?.size

        assertEquals(VaultStoreResult.DONE, store.destroy())

        assertEquals(listOf(DELETE_TEMP, OVERWRITE, DELETE), ops.calls)
        assertNull(ops.vaultBytes)
        assertTrue((sizeBefore ?: 0) > 0)
    }

    @Test
    fun destroy_overwrite_leavesZerosOfSameLength() = runTest {
        val store = store()
        store.write(vault())
        val length = ops.vaultBytes?.size ?: 0
        ops.failingStep = DELETE

        assertEquals(VaultStoreResult.FAILED, store.destroy())

        assertContentEquals(ByteArray(length), ops.vaultBytes)
    }

    @Test
    fun destroy_overwriteFails_stillTriesDeleteAndReportsFailed() = runTest {
        val store = store()
        store.write(vault())
        ops.failingStep = OVERWRITE
        ops.calls.clear()

        assertEquals(VaultStoreResult.FAILED, store.destroy())

        assertEquals(listOf(DELETE_TEMP, OVERWRITE, DELETE), ops.calls)
        assertNull(ops.vaultBytes)
    }

    @Test
    fun destroy_noFile_done() = runTest {
        assertEquals(VaultStoreResult.DONE, store().destroy())
    }

    @Test
    fun readResult_ok_toStringIsRedacted() = runTest {
        val store = store()
        store.write(vault())

        assertEquals("Ok(VaultFile(format=1))", store.read().toString())
        assertEquals("Content(***)", FileRead.Content(ByteArray(1)).toString())
    }

    private companion object {
        const val READ = "read"
        const val WRITE_TEMP = "writeTemp"
        const val REPLACE = "replace"
        const val SYNC_DIRECTORY = "syncDirectory"
        const val DELETE_TEMP = "deleteTemp"
        const val OVERWRITE = "overwrite"
        const val DELETE = "delete"
        const val WRAPPED_PASSWORD_BYTES = 88
        const val WRAPPED_RECOVERY_BYTES = 60
    }
}
