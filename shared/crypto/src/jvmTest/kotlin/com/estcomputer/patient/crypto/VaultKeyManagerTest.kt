package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupCommitResult
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VaultKeyManagerTest {
    private fun chars(text: String) = SecretChars.wrap(text.toCharArray())

    private suspend fun VaultRig.setUp() {
        assertIs<SetupResult.Ready>(manager.prepareSetup(chars(PASSWORD))).pending.commit()
    }

    private suspend fun VaultRig.vault() = assertIs<VaultReadResult.Ok>(store.read()).vault

    @Test
    fun lifecycle_setup_lock_unlock() = runTest {
        val rig = VaultRig(this)
        assertEquals(StartupState.Clean, rig.manager.startupState())
        rig.setUp()
        assertEquals(StartupState.VaultPresent, rig.manager.startupState())

        rig.manager.lock()
        assertFailsWith<IllegalStateException> { rig.manager.databaseKey() }
        assertEquals(
            UnlockResult.Unlocked(needsBiometricRefresh = false),
            rig.manager.unlockWithPassword(chars(PASSWORD)),
        )

        assertEquals(CryptoConstants.KEY_SIZE_BYTES, rig.manager.databaseKey().use { it.size })
    }

    @Test
    fun keys_areFreshCopiesAndDiffer() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        val first = rig.manager.databaseKey()
        val second = rig.manager.databaseKey()
        val fkek = rig.manager.fileKeyEncryptionKey()

        first.zero()

        assertFalse(second.isZeroed)
        assertFalse(second.read { it.contentEquals(fkek.read { f -> f.copyOf() }) })
        assertContentEquals(second.read { it.copyOf() }, rig.manager.databaseKey().use { it.copyOf() })
    }

    @Test
    fun lock_isIdempotentAndZeroesMk() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        val mk = rig.session.requireMk()

        rig.manager.lock()
        rig.manager.lock()

        assertTrue(mk.isZeroed)
        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun lock_abandonsAPendingSetup() = runTest {
        val rig = VaultRig(this)
        val pending = assertIs<SetupResult.Ready>(rig.manager.prepareSetup(chars(PASSWORD))).pending
        val words = pending.recoveryWords

        rig.manager.lock()

        assertTrue(words.isZeroed)
        assertNull(rig.files.vaultBytes)
    }

    @Test
    fun prepareSetup_replacesAnEarlierPendingSetup() = runTest {
        val rig = VaultRig(this)
        val first = assertIs<SetupResult.Ready>(rig.manager.prepareSetup(chars(PASSWORD))).pending
        val firstWords = first.recoveryWords

        rig.manager.prepareSetup(chars(PASSWORD))

        assertTrue(firstWords.isZeroed)
    }

    @Test
    fun destroyKeys_removesKeysBeforeTheVaultFile() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.bioKeystore.keys[CryptoConstants.BIO_KEY_ALIAS] = ByteArray(1)

        rig.manager.destroyKeys()

        assertTrue(rig.keystore.keys.isEmpty())
        assertTrue(rig.bioKeystore.keys.isEmpty())
        assertNull(rig.files.vaultBytes)
        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun destroyKeys_fileEraseFails_keysAreStillGone() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.files.failingStep = "delete"

        assertFailsWith<IllegalStateException> { rig.manager.destroyKeys() }

        assertTrue(rig.keystore.keys.isEmpty())
    }

    @Test
    fun autoWipe_throughTheRealDestroyKeys_doesNotDeadlock() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.manager.setWipeAfterFailedAttempts(10)
        rig.store.write(rig.vault().copy(failedAttempts = 9))
        rig.manager.lock()
        rig.wipe.onWipe = { rig.manager.destroyKeys() }

        assertEquals(UnlockResult.Wiped, rig.manager.unlockWithPassword(chars("wrong password")))

        assertNull(rig.files.vaultBytes)
        assertTrue(rig.keystore.keys.isEmpty())
    }

    @Test
    fun setWipeAfterFailedAttempts_persistsAndCanBeTurnedOff() = runTest {
        val rig = VaultRig(this)
        rig.setUp()

        rig.manager.setWipeAfterFailedAttempts(15)
        assertEquals(15, rig.vault().wipeAfterFailedAttempts)
        rig.manager.setWipeAfterFailedAttempts(null)
        assertEquals(0, rig.vault().wipeAfterFailedAttempts)
    }

    @Test
    fun setWipeAfterFailedAttempts_outOfRange_isRejected() = runTest {
        val rig = VaultRig(this)
        rig.setUp()

        assertFailsWith<IllegalArgumentException> { rig.manager.setWipeAfterFailedAttempts(9) }
        assertFailsWith<IllegalArgumentException> { rig.manager.setWipeAfterFailedAttempts(21) }
    }

    @Test
    fun setWipeAfterFailedAttempts_whileLocked_isACallerBug() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.manager.lock()

        assertFailsWith<IllegalStateException> { rig.manager.setWipeAfterFailedAttempts(10) }
    }

    @Test
    fun commitFailure_keepsTheManagerLockedAndClean() = runTest {
        val rig = VaultRig(this)
        val pending = assertIs<SetupResult.Ready>(rig.manager.prepareSetup(chars(PASSWORD))).pending
        rig.files.failingStep = "temp"

        assertEquals(SetupCommitResult.Failed(KeyError.STORAGE_FAILED), pending.commit())

        assertFailsWith<IllegalStateException> { rig.manager.databaseKey() }
    }

    private companion object {
        const val PASSWORD = "correct horse battery"
    }
}
