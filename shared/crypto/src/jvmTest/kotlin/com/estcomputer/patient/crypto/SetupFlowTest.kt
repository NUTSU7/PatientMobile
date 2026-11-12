package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.PasswordRejection
import com.estcomputer.patient.domain.model.PendingSetup
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupCommitResult
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SetupFlowTest {
    private fun chars(text: String) = SecretChars.wrap(text.toCharArray())

    private suspend fun VaultRig.pending(password: String = PASSWORD): PendingSetup {
        val result = setup.prepare(chars(password))
        return assertIs<SetupResult.Ready>(result).pending
    }

    private fun TestScope.rig() = VaultRig(this)

    @Test
    fun startup_wipeMarker_isWipeInterrupted() = runTest {
        val rig = rig().apply { probe.hasMarker = true }

        assertEquals(StartupState.WipeInterrupted, rig.inspector.inspect())
    }

    @Test
    fun startup_markerWinsOverVault() = runTest {
        val rig = rig()
        rig.pending().commit()
        rig.probe.hasMarker = true

        assertEquals(StartupState.WipeInterrupted, rig.inspector.inspect())
    }

    @Test
    fun startup_vaultPresent_isVaultPresent() = runTest {
        val rig = rig()
        rig.pending().commit()

        assertEquals(StartupState.VaultPresent, rig.inspector.inspect())
    }

    @Test
    fun startup_unreadableVaultFile_isVaultPresent() = runTest {
        val rig = rig()
        rig.pending().commit()
        rig.files.failingStep = "read"

        assertEquals(StartupState.VaultPresent, rig.inspector.inspect())
    }

    @Test
    fun startup_corruptVaultFile_isVaultPresent() = runTest {
        val rig = rig().apply { files.vaultBytes = "{}".encodeToByteArray() }

        assertEquals(StartupState.VaultPresent, rig.inspector.inspect())
    }

    @Test
    fun startup_dataWithoutVault_isOrphanedAndKeepsKeys() = runTest {
        val rig = rig().apply {
            probe.hasData = true
            keystore.keys[CryptoConstants.HW_KEY_ALIAS] = ByteArray(1)
        }

        assertEquals(StartupState.OrphanedData, rig.inspector.inspect())
        assertTrue(CryptoConstants.HW_KEY_ALIAS in rig.keystore.keys)
    }

    @Test
    fun startup_nothingPresent_isCleanAndDeletesStaleAliases() = runTest {
        val rig = rig().apply {
            keystore.keys[CryptoConstants.HW_KEY_ALIAS] = ByteArray(1)
            bioKeystore.keys[CryptoConstants.BIO_KEY_ALIAS] = ByteArray(1)
        }

        assertEquals(StartupState.Clean, rig.inspector.inspect())
        assertTrue(rig.keystore.keys.isEmpty())
        assertTrue(rig.bioKeystore.keys.isEmpty())
    }

    @Test
    fun prepare_deviceNotSecure_failsBeforeAnyWork() = runTest {
        val rig = rig().apply { security.isSecure = false }

        val result = rig.setup.prepare(chars(PASSWORD))

        assertEquals(SetupResult.Failed(KeyError.DEVICE_NOT_SECURE), result)
        assertEquals(0, rig.kdf.calibrateCalls)
    }

    @Test
    fun prepare_weakPassword_isRejected() = runTest {
        val rig = rig()

        assertEquals(
            SetupResult.PasswordRejected(PasswordRejection.TOO_SHORT),
            rig.setup.prepare(chars("short")),
        )
        assertEquals(
            SetupResult.PasswordRejected(PasswordRejection.COMMON),
            rig.setup.prepare(chars("password")),
        )
    }

    @Test
    fun prepare_outsideCleanState_isACallerBug() = runTest {
        val rig = rig()
        rig.pending().commit()

        assertFailsWith<IllegalStateException> { rig.setup.prepare(chars(PASSWORD)) }
    }

    @Test
    fun prepare_kdfFailure_returnsItsError() = runTest {
        val rig = rig().apply { kdf.failure = KeyError.OUT_OF_MEMORY }

        assertEquals(SetupResult.Failed(KeyError.OUT_OF_MEMORY), rig.setup.prepare(chars(PASSWORD)))
    }

    @Test
    fun prepare_touchesNoDisk() = runTest {
        val rig = rig()
        rig.pending()

        assertNull(rig.files.vaultBytes)
        assertEquals(0, rig.files.writes)
        assertEquals(0, rig.keystore.generateCalls)
    }

    @Test
    fun prepare_returnsTwentyFourWords() = runTest {
        val words = rig().pending().recoveryWords.use { String(it) }

        assertEquals(24, words.split(" ").size)
    }

    @Test
    fun commit_writesExactlyOneVaultAndUnlocks() = runTest {
        val rig = rig()

        assertEquals(SetupCommitResult.Committed, rig.pending().commit())

        assertEquals(1, rig.files.writes)
        assertTrue(rig.session.isUnlocked)
        assertEquals(1, rig.keystore.generateCalls)
        val vault = assertIs<VaultReadResult.Ok>(rig.store.read()).vault
        assertEquals(0, vault.failedAttempts)
        assertNull(vault.wrappedMkBiometric)
    }

    @Test
    fun commit_zeroesTheWords() = runTest {
        val pending = rig().pending()
        val words = pending.recoveryWords

        pending.commit()

        assertTrue(words.isZeroed)
    }

    @Test
    fun commit_writeFails_dropsHwKeyAndLeavesNoVault() = runTest {
        val rig = rig()
        val pending = rig.pending()
        rig.files.failingStep = "temp"

        assertEquals(SetupCommitResult.Failed(KeyError.STORAGE_FAILED), pending.commit())

        assertTrue(rig.keystore.keys.isEmpty())
        assertFalse(rig.session.isUnlocked)
        assertNull(rig.files.vaultBytes)
    }

    @Test
    fun commit_afterFailure_canBeRetried() = runTest {
        val rig = rig()
        val pending = rig.pending()
        rig.files.failingStep = "temp"
        pending.commit()
        rig.files.failingStep = null

        assertEquals(SetupCommitResult.Committed, pending.commit())
    }

    @Test
    fun commit_deviceBecameInsecure_reportsIt() = runTest {
        val rig = rig()
        val pending = rig.pending()
        rig.keystore.isDeviceSecure = false

        assertEquals(SetupCommitResult.Failed(KeyError.DEVICE_NOT_SECURE), pending.commit())
        assertNull(rig.files.vaultBytes)
    }

    @Test
    fun commit_hwSealFails_dropsKey() = runTest {
        val rig = rig()
        val pending = rig.pending()
        rig.keystore.sealError = KeystoreError.DEVICE_LOCKED

        assertEquals(SetupCommitResult.Failed(KeyError.DEVICE_LOCKED), pending.commit())
        assertTrue(rig.keystore.keys.isEmpty())
    }

    @Test
    fun commit_twice_isACallerBug() = runTest {
        val pending = rig().pending()
        pending.commit()

        assertFailsWith<IllegalStateException> { pending.commit() }
    }

    @Test
    fun abandon_leavesNoFilesAndNoKeys() = runTest {
        val rig = rig()
        val pending = rig.pending()
        val words = pending.recoveryWords

        pending.abandon()
        pending.abandon()

        assertTrue(words.isZeroed)
        assertNull(rig.files.vaultBytes)
        assertTrue(rig.keystore.keys.isEmpty())
        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun abandon_whileAnotherOperationHoldsTheLock_zeroesWordsNowAndCommitIsRefused() = runTest {
        val rig = rig()
        val pending = rig.pending()
        rig.mutex.lock()

        pending.abandon()

        assertTrue(pending.recoveryWords.isZeroed)
        rig.mutex.unlock()
        assertFailsWith<IllegalStateException> { pending.commit() }
        assertNull(rig.files.vaultBytes)
    }

    @Test
    fun prepare_whenAStepThrows_releasesTheMasterKeyAndLeavesNothing() = runTest {
        val rig = rig()
        rig.x25519.isBroken = true

        assertFailsWith<IllegalStateException> { rig.setup.prepare(chars(PASSWORD)) }

        assertNull(rig.files.vaultBytes)
        rig.x25519.isBroken = false
        assertIs<SetupResult.Ready>(rig.setup.prepare(chars(PASSWORD)))
    }

    @Test
    fun session_afterCommit_holdsMkThatTheRecoveryBlobOpens() = runTest {
        val rig = rig()
        val words = rig.pending().let { p -> p.recoveryWords.read { String(it) }.also { p.commit() } }
        val vault = assertIs<VaultReadResult.Ok>(rig.store.read()).vault
        val decoded = rig.bip39.decode(chars(words))
        assertIs<DecodeResult.Ok>(decoded)
        val rwk = rig.keyDerivation.deriveRwk(decoded.entropy)

        val opened = rig.aead.open(rwk, vault.wrappedMkRecovery, CryptoConstants.AAD_MK_RECOVERY)

        assertIs<OpenResult.Ok>(opened)
        assertNotNull(opened.plaintext.read { it.copyOf() })
        assertTrue(opened.plaintext.read { mk -> rig.session.requireMk().read { it.contentEquals(mk) } })
    }

    private companion object {
        const val PASSWORD = "correct horse battery"
    }
}
