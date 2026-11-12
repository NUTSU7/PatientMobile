package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.PasswordRejection
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.UnlockResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecoveryFlowTest {
    private class Setup(val rig: VaultRig, val words: String, val mk: ByteArray)

    private fun chars(text: String) = SecretChars.wrap(text.toCharArray())

    private fun TestScope.rig() = VaultRig(this)

    private suspend fun lockedSetup(rig: VaultRig): Setup {
        val pending = assertIs<SetupResult.Ready>(rig.setup.prepare(chars(PASSWORD))).pending
        val words = pending.recoveryWords.read { String(it) }
        pending.commit()
        val mk = rig.session.requireMk().read { it.copyOf() }
        rig.session.clear()
        return Setup(rig, words, mk)
    }

    private suspend fun VaultRig.vault() = assertIs<VaultReadResult.Ok>(store.read()).vault

    private fun otherWords(rig: VaultRig): String =
        rig.bip39.encode(SecretBytes.wrap(ByteArray(CryptoConstants.KEY_SIZE_BYTES) { 7 })).read { String(it) }

    @Test
    fun recovery_forgottenPassword_setsANewOneAndKeepsMk() = runTest {
        val s = lockedSetup(rig())
        val before = s.rig.vault()

        val result = s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD))

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), result)
        assertContentEquals(s.mk, s.rig.session.requireMk().read { it.copyOf() })
        val after = s.rig.vault()
        assertContentEquals(before.wrappedMkRecovery, after.wrappedMkRecovery)
        assertContentEquals(before.exportPublicKey, after.exportPublicKey)
        s.rig.session.clear()
        assertEquals(UnlockResult.Failed(KeyError.WRONG_PASSWORD), s.rig.unlock.unlock(chars(PASSWORD)))
        s.rig.boot.elapsedMillis += SKIP
        assertEquals(
            UnlockResult.Unlocked(needsBiometricRefresh = false),
            s.rig.unlock.unlock(chars(NEW_PASSWORD)),
        )
    }

    @Test
    fun recovery_usesANewSaltAndResetsTheCounter() = runTest {
        val s = lockedSetup(rig())
        s.rig.store.write(s.rig.vault().copy(failedAttempts = 6, lockoutUntilElapsed = 9, lockoutBootCount = 1))
        val oldSalt = s.rig.vault().kdf.salt.copyOf()

        s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD))

        val vault = s.rig.vault()
        assertEquals(0, vault.failedAttempts)
        assertEquals(0, vault.lockoutUntilElapsed)
        assertFalse(oldSalt.contentEquals(vault.kdf.salt))
    }

    @Test
    fun recovery_hardwareKeyGone_rebuildsIt() = runTest {
        val s = lockedSetup(rig())
        s.rig.keystore.keys.clear()
        val generatedBefore = s.rig.keystore.generateCalls

        val result = s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD))

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), result)
        assertEquals(generatedBefore + 1, s.rig.keystore.generateCalls)
        s.rig.session.clear()
        assertEquals(
            UnlockResult.Unlocked(needsBiometricRefresh = false),
            s.rig.unlock.unlock(chars(NEW_PASSWORD)),
        )
    }

    @Test
    fun recovery_hardwareKeyInvalidated_rebuildsIt() = runTest {
        val s = lockedSetup(rig())
        s.rig.keystore.failNextSeal = KeystoreError.HW_KEY_INVALIDATED

        val result = s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD))

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), result)
        assertTrue(CryptoConstants.HW_KEY_ALIAS in s.rig.keystore.deleted)
    }

    @Test
    fun recovery_deviceNotSecureWhenRebuilding_failsAndKeepsVault() = runTest {
        val s = lockedSetup(rig())
        val before = s.rig.storedBytes()
        s.rig.keystore.keys.clear()
        s.rig.keystore.isDeviceSecure = false

        assertEquals(
            UnlockResult.Failed(KeyError.DEVICE_NOT_SECURE),
            s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD)),
        )
        assertContentEquals(before, s.rig.files.vaultBytes)
        assertFalse(s.rig.session.isUnlocked)
    }

    @Test
    fun recovery_deviceLocked_doesNotRebuildTheKey() = runTest {
        val s = lockedSetup(rig())
        s.rig.keystore.failNextSeal = KeystoreError.DEVICE_LOCKED
        val generatedBefore = s.rig.keystore.generateCalls

        assertEquals(
            UnlockResult.Failed(KeyError.DEVICE_LOCKED),
            s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD)),
        )
        assertEquals(generatedBefore, s.rig.keystore.generateCalls)
    }

    @Test
    fun recovery_badChecksum_isRejectedBeforeAnyKeyWork() = runTest {
        val s = lockedSetup(rig())
        val parts = s.words.split(" ")
        val swapped = listOf(parts[1], parts[0]) + parts.drop(2)

        val result = s.rig.recovery.unlock(chars(swapped.joinToString(" ")), chars(NEW_PASSWORD))

        assertEquals(UnlockResult.Failed(KeyError.WRONG_RECOVERY_WORDS), result)
        assertEquals(1, s.rig.kdf.calibrateCalls)
        // A typo costs nothing: a correct attempt right after is accepted.
        assertIs<UnlockResult.Unlocked>(s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD)))
    }

    @Test
    fun recovery_wrongWordCount_isRejected() = runTest {
        val s = lockedSetup(rig())

        assertEquals(
            UnlockResult.Failed(KeyError.WRONG_RECOVERY_WORDS),
            s.rig.recovery.unlock(chars("abandon abandon"), chars(NEW_PASSWORD)),
        )
    }

    @Test
    fun recovery_wrongButValidWords_isRateLimitedInMemory() = runTest {
        val s = lockedSetup(rig())
        val wrong = otherWords(s.rig)
        repeat(5) {
            s.rig.boot.elapsedMillis += SKIP
            assertEquals(
                UnlockResult.Failed(KeyError.WRONG_RECOVERY_WORDS),
                s.rig.recovery.unlock(chars(wrong), chars(NEW_PASSWORD)),
            )
        }

        assertIs<UnlockResult.LockedOut>(s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD)))
        s.rig.boot.elapsedMillis += SKIP
        assertIs<UnlockResult.Unlocked>(s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD)))
    }

    @Test
    fun recovery_wrongWords_neverTouchTheVaultOrWipe() = runTest {
        val s = lockedSetup(rig())
        s.rig.store.write(s.rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))
        val before = s.rig.storedBytes()

        repeat(3) {
            s.rig.boot.elapsedMillis += SKIP
            s.rig.recovery.unlock(chars(otherWords(s.rig)), chars(NEW_PASSWORD))
        }

        assertContentEquals(before, s.rig.files.vaultBytes)
        assertEquals(0, s.rig.wipe.calls)
    }

    @Test
    fun recovery_weakNewPassword_isRejectedAndNothingChanges() = runTest {
        val s = lockedSetup(rig())
        val before = s.rig.storedBytes()

        assertEquals(
            UnlockResult.PasswordRejected(PasswordRejection.TOO_SHORT),
            s.rig.recovery.unlock(chars(s.words), chars("short")),
        )
        assertContentEquals(before, s.rig.files.vaultBytes)
        assertFalse(s.rig.session.isUnlocked)
    }

    @Test
    fun recovery_clearsBiometrics() = runTest {
        val s = lockedSetup(rig())
        val bio = ByteArray(CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.KEY_SIZE_BYTES + 40)
        s.rig.store.write(s.rig.vault().copy(wrappedMkBiometric = bio, bioKeyAlias = CryptoConstants.BIO_KEY_ALIAS))
        s.rig.bioKeystore.keys[CryptoConstants.BIO_KEY_ALIAS] = ByteArray(1)

        s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD))

        val vault = s.rig.vault()
        assertNull(vault.wrappedMkBiometric)
        assertNull(vault.bioKeyAlias)
        assertTrue(s.rig.bioKeystore.keys.isEmpty())
    }

    @Test
    fun recovery_writeFails_leavesOldVaultAndBiometricsAlone() = runTest {
        val s = lockedSetup(rig())
        val before = s.rig.storedBytes()
        s.rig.bioKeystore.keys[CryptoConstants.BIO_KEY_ALIAS] = ByteArray(1)
        s.rig.files.failingStep = "temp"

        assertEquals(
            UnlockResult.Failed(KeyError.STORAGE_FAILED),
            s.rig.recovery.unlock(chars(s.words), chars(NEW_PASSWORD)),
        )
        assertContentEquals(before, s.rig.files.vaultBytes)
        assertTrue(CryptoConstants.BIO_KEY_ALIAS in s.rig.bioKeystore.keys)
    }

    @Test
    fun recovery_noVault_isVaultCorrupt() = runTest {
        val rig = rig()

        assertEquals(
            UnlockResult.Failed(KeyError.VAULT_CORRUPT),
            rig.recovery.unlock(chars(otherWords(rig)), chars(NEW_PASSWORD)),
        )
    }

    private companion object {
        const val PASSWORD = "correct horse battery"
        const val NEW_PASSWORD = "another long passphrase"
        const val SKIP = 4_000_000L
    }
}
