package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupCommitResult
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.model.WipeSettingResult
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

        assertEquals(KeyDestructionResult.DONE, rig.manager.destroyKeys())

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

        assertEquals(KeyDestructionResult.VAULT_FILE_REMAINS, rig.manager.destroyKeys())

        assertTrue(rig.keystore.keys.isEmpty())
    }

    @Test
    fun autoWipe_throughTheRealDestroyKeys_doesNotDeadlock() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.manager.setWipeAfterFailedAttempts(10, null)
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

        assertEquals(WipeSettingResult.Saved, rig.manager.setWipeAfterFailedAttempts(15, null))
        assertEquals(15, rig.vault().wipeAfterFailedAttempts)
        assertEquals(WipeSettingResult.Saved, rig.manager.setWipeAfterFailedAttempts(null, null))
        assertEquals(0, rig.vault().wipeAfterFailedAttempts)
    }

    @Test
    fun setWipeAfterFailedAttempts_writeFails_reportsItAndKeepsTheOldValue() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.files.failingStep = "temp"

        assertEquals(
            WipeSettingResult.Failed(KeyError.STORAGE_FAILED),
            rig.manager.setWipeAfterFailedAttempts(15, null),
        )

        assertEquals(0, rig.vault().wipeAfterFailedAttempts)
    }

    @Test
    fun setWipeAfterFailedAttempts_outOfRange_isRejected() = runTest {
        val rig = VaultRig(this)
        rig.setUp()

        assertFailsWith<IllegalArgumentException> { rig.manager.setWipeAfterFailedAttempts(9, null) }
        assertFailsWith<IllegalArgumentException> { rig.manager.setWipeAfterFailedAttempts(21, null) }
    }

    @Test
    fun setWipe_afterBiometricUnlockWithoutPassword_requiresPassword() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        val mk = rig.session.requireMk().read { SecretBytes.copyOf(it) }
        rig.session.start(mk, rig.clock.now(), passwordElapsedMillis = null)

        assertEquals(
            WipeSettingResult.Failed(KeyError.PASSWORD_REQUIRED),
            rig.manager.setWipeAfterFailedAttempts(10, null),
        )

        assertEquals(0, rig.vault().wipeAfterFailedAttempts)
    }

    @Test
    fun setWipe_after61Seconds_requiresPassword() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.boot.elapsedMillis += 61_000

        assertEquals(
            WipeSettingResult.Failed(KeyError.PASSWORD_REQUIRED),
            rig.manager.setWipeAfterFailedAttempts(10, null),
        )
    }

    @Test
    fun setWipe_whileLockedOut_reportsRemainingTime() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.store.write(
            rig.vault().copy(
                failedAttempts = 5,
                lockoutUntilElapsed = rig.boot.elapsedMillis + 30_000,
                lockoutBootCount = rig.boot.bootCount,
            ),
        )

        assertEquals(
            WipeSettingResult.LockedOut(30_000),
            rig.manager.setWipeAfterFailedAttempts(10, chars(PASSWORD)),
        )
    }

    @Test
    fun setWipe_kdfOutOfMemory_reportsTheKeyError() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.kdf.failure = KeyError.OUT_OF_MEMORY

        assertEquals(
            WipeSettingResult.Failed(KeyError.OUT_OF_MEMORY),
            rig.manager.setWipeAfterFailedAttempts(10, chars(PASSWORD)),
        )
        assertEquals(0, rig.vault().failedAttempts)
    }

    @Test
    fun setWipe_withCorrectPassword_saves() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.boot.elapsedMillis += 61_000

        assertEquals(WipeSettingResult.Saved, rig.manager.setWipeAfterFailedAttempts(12, chars(PASSWORD)))

        assertEquals(12, rig.vault().wipeAfterFailedAttempts)
        assertEquals(0, rig.vault().failedAttempts)
    }

    @Test
    fun setWipe_withWrongPassword_countsAttempt() = runTest {
        val rig = VaultRig(this)
        rig.setUp()

        val result = rig.manager.setWipeAfterFailedAttempts(null, chars("wrong password"))

        assertEquals(WipeSettingResult.Failed(KeyError.WRONG_PASSWORD), result)
        assertEquals(1, rig.vault().failedAttempts)
    }

    @Test
    fun setWipe_wrongPasswordAtLimit_wipes() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))
        rig.wipe.onWipe = { rig.manager.destroyKeys() }

        assertEquals(WipeSettingResult.Wiped, rig.manager.setWipeAfterFailedAttempts(null, chars("wrong password")))

        assertEquals(1, rig.wipe.calls)
        assertNull(rig.files.vaultBytes)
    }

    @Test
    fun setWipeAfterFailedAttempts_whileLocked_isACallerBug() = runTest {
        val rig = VaultRig(this)
        rig.setUp()
        rig.manager.lock()

        assertFailsWith<IllegalStateException> { rig.manager.setWipeAfterFailedAttempts(10, null) }
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
