package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.BiometricEnrolmentResult
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BiometricChallenge
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class BiometricFlowTest {
    private fun chars(text: String) = SecretChars.wrap(text.toCharArray())

    private suspend fun unlockedRig(scope: TestScope): VaultRig {
        val rig = VaultRig(scope)
        assertIs<SetupResult.Ready>(rig.setup.prepare(chars(PASSWORD))).pending.commit()
        return rig
    }

    private suspend fun VaultRig.vault() = assertIs<VaultReadResult.Ok>(store.read()).vault

    private suspend fun VaultRig.challenge(result: BiometricBeginResult): BiometricChallenge =
        assertIs<BiometricBeginResult.Ready>(result).challenge

    private suspend fun VaultRig.enable() {
        val challenge = challenge(biometrics.beginBiometricEnrolment())
        assertEquals(BiometricEnrolmentResult.Enabled, biometrics.completeBiometricEnrolment(challenge))
    }

    private suspend fun VaultRig.bioUnlock(): UnlockResult =
        biometrics.completeBiometricUnlock(challenge(biometrics.beginBiometricUnlock()))

    private fun VaultRig.mk() = session.requireMk().read { it.copyOf() }

    @Test
    fun enable_lock_bioUnlock_roundTripsMk() = runTest {
        val rig = unlockedRig(this)
        val mk = rig.mk()
        rig.enable()
        rig.session.clear()

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), rig.bioUnlock())

        assertContentEquals(mk, rig.mk())
    }

    @Test
    fun enable_storesBlobAndAlias() = runTest {
        val rig = unlockedRig(this)

        rig.enable()

        val vault = rig.vault()
        assertEquals(CryptoConstants.BIO_KEY_ALIAS, vault.bioKeyAlias)
        assertNotNull(vault.wrappedMkBiometric)
    }

    @Test
    fun enable_noBiometricEnrolled_failsAndLeavesVault() = runTest {
        val rig = unlockedRig(this)
        rig.bioKeystore.isEnrolled = false
        val before = rig.storedBytes()

        assertEquals(
            BiometricBeginResult.Failed(KeyError.BIOMETRIC_NOT_ENROLLED),
            rig.biometrics.beginBiometricEnrolment(),
        )
        assertContentEquals(before, rig.files.vaultBytes)
    }

    @Test
    fun enable_whileLocked_isACallerBug() = runTest {
        val rig = unlockedRig(this)
        rig.session.clear()

        assertFailsWith<IllegalStateException> { rig.biometrics.beginBiometricEnrolment() }
    }

    @Test
    fun refresh_cancelled_keepsTheOldBlob() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        val before = requireNotNull(rig.vault().wrappedMkBiometric).copyOf()

        rig.challenge(rig.biometrics.beginBiometricEnrolment())

        assertContentEquals(before, rig.vault().wrappedMkBiometric)
    }

    @Test
    fun refresh_afterPasswordUnlock_restartsTheSevenDays() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.clock.advance(6.days)
        rig.boot.elapsedMillis += SKIP
        rig.unlock.unlock(chars(PASSWORD))
        rig.enable()
        rig.session.clear()
        rig.clock.advance(6.days)

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), rig.bioUnlock())
    }

    @Test
    fun refresh_reusesTheExistingKey() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        val key = rig.bioKeystore.keys.getValue(CryptoConstants.BIO_KEY_ALIAS).copyOf()

        rig.enable()

        assertContentEquals(key, rig.bioKeystore.keys.getValue(CryptoConstants.BIO_KEY_ALIAS))
    }

    @Test
    fun refresh_keyMissing_createsANewOne() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.bioKeystore.keys.clear()

        rig.enable()

        assertTrue(CryptoConstants.BIO_KEY_ALIAS in rig.bioKeystore.keys)
    }

    @Test
    fun bioUnlock_afterWrongPasswords_clearsCounterAndLockout() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        repeat(5) { rig.unlock.unlock(chars("wrong password")) }
        assertEquals(5, rig.vault().failedAttempts)

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), rig.bioUnlock())

        assertEquals(0, rig.vault().failedAttempts)
        assertEquals(0, rig.vault().lockoutUntilElapsed)
    }

    @Test
    fun bioUnlock_withoutFailedAttempts_writesNothing() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        val writes = rig.files.writes

        rig.bioUnlock()

        assertEquals(writes, rig.files.writes)
    }

    @Test
    fun bioUnlock_policyExpired_keepsCounter() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.unlock.unlock(chars("wrong password"))
        rig.boot.bootCount = 2

        rig.bioUnlock()

        assertEquals(1, rig.vault().failedAttempts)
    }

    @Test
    fun bioUnlock_afterReboot_requiresPassword() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.boot.bootCount = 2

        assertEquals(UnlockResult.Failed(KeyError.BIOMETRIC_POLICY_EXPIRED), rig.bioUnlock())

        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun bioUnlock_exactlySevenDays_isAllowed() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.clock.advance(7.days)

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), rig.bioUnlock())
    }

    @Test
    fun bioUnlock_overSevenDays_requiresPassword() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.clock.advance(7.days + 1.seconds)

        assertEquals(UnlockResult.Failed(KeyError.BIOMETRIC_POLICY_EXPIRED), rig.bioUnlock())
    }

    @Test
    fun bioUnlock_clockMovedBack_requiresPassword() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.clock.advance((-1).days)

        assertEquals(UnlockResult.Failed(KeyError.BIOMETRIC_POLICY_EXPIRED), rig.bioUnlock())
    }

    @Test
    fun bioUnlock_neverExtendsThePasswordWindow() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.clock.advance(6.days)
        assertIs<UnlockResult.Unlocked>(rig.bioUnlock())
        // Re-enabling while unlocked by biometrics must keep the original password time.
        rig.enable()
        rig.session.clear()
        rig.clock.advance(2.days)

        assertEquals(UnlockResult.Failed(KeyError.BIOMETRIC_POLICY_EXPIRED), rig.bioUnlock())
    }

    @Test
    fun bioUnlock_keyInvalidated_clearsEverything() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.bioKeystore.beginError = BiometricKeystoreError.KEY_INVALIDATED

        assertEquals(
            BiometricBeginResult.Failed(KeyError.BIOMETRIC_INVALIDATED),
            rig.biometrics.beginBiometricUnlock(),
        )

        val vault = rig.vault()
        assertNull(vault.wrappedMkBiometric)
        assertNull(vault.bioKeyAlias)
        assertTrue(CryptoConstants.BIO_KEY_ALIAS in rig.bioKeystore.deleted)
    }

    @Test
    fun bioUnlock_whenNotEnabled_isInvalidated() = runTest {
        val rig = unlockedRig(this)
        rig.session.clear()

        assertEquals(
            BiometricBeginResult.Failed(KeyError.BIOMETRIC_INVALIDATED),
            rig.biometrics.beginBiometricUnlock(),
        )
    }

    @Test
    fun bioUnlock_blobRejected_clears() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.bioKeystore.openError = BiometricKeystoreError.BLOB_REJECTED

        assertEquals(UnlockResult.Failed(KeyError.BIOMETRIC_INVALIDATED), rig.bioUnlock())

        assertNull(rig.vault().wrappedMkBiometric)
    }

    @Test
    fun bioUnlock_notAuthenticated_keepsTheBlob() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        rig.bioKeystore.openError = BiometricKeystoreError.NOT_AUTHENTICATED

        assertEquals(UnlockResult.Failed(KeyError.BIOMETRIC_INVALIDATED), rig.bioUnlock())

        assertNotNull(rig.vault().wrappedMkBiometric)
    }

    @Test
    fun bioUnlock_malformedPayload_clears() = runTest {
        val rig = unlockedRig(this)
        rig.enable()
        rig.session.clear()
        val challenge = assertIs<BiometricKeystoreBeginResult.Ready>(
            rig.bioKeystore.beginSeal(CryptoConstants.BIO_KEY_ALIAS),
        ).challenge
        val garbage = assertIs<BiometricKeystoreSealResult.Ok>(
            rig.bioKeystore.seal(challenge, SecretBytes.wrap(ByteArray(GARBAGE_BYTES)), CryptoConstants.AAD_MK_BIO),
        )
        rig.store.write(rig.vault().copy(wrappedMkBiometric = garbage.blob))

        assertEquals(UnlockResult.Failed(KeyError.BIOMETRIC_INVALIDATED), rig.bioUnlock())

        assertNull(rig.vault().bioKeyAlias)
    }

    @Test
    fun enable_sealFailsAsInvalidated_clears() = runTest {
        val rig = unlockedRig(this)
        val challenge = rig.challenge(rig.biometrics.beginBiometricEnrolment())
        rig.bioKeystore.sealError = BiometricKeystoreError.KEY_INVALIDATED

        assertEquals(
            BiometricEnrolmentResult.Failed(KeyError.BIOMETRIC_INVALIDATED),
            rig.biometrics.completeBiometricEnrolment(challenge),
        )
        assertTrue(CryptoConstants.BIO_KEY_ALIAS in rig.bioKeystore.deleted)
    }

    @Test
    fun enable_writeFails_reportsStorageFailure() = runTest {
        val rig = unlockedRig(this)
        val challenge = rig.challenge(rig.biometrics.beginBiometricEnrolment())
        rig.files.failingStep = "temp"

        assertEquals(
            BiometricEnrolmentResult.Failed(KeyError.STORAGE_FAILED),
            rig.biometrics.completeBiometricEnrolment(challenge),
        )
    }

    @Test
    fun disable_deletesKeyAndClearsFields() = runTest {
        val rig = unlockedRig(this)
        rig.enable()

        rig.biometrics.disableBiometrics()

        val vault = rig.vault()
        assertNull(vault.wrappedMkBiometric)
        assertNull(vault.bioKeyAlias)
        assertTrue(rig.bioKeystore.keys.isEmpty())
    }

    @Test
    fun disable_whenNeverEnabled_isHarmless() = runTest {
        val rig = unlockedRig(this)
        val before = rig.storedBytes()

        rig.biometrics.disableBiometrics()

        assertContentEquals(before, rig.files.vaultBytes)
    }

    private companion object {
        const val PASSWORD = "correct horse battery"
        const val SKIP = 4_000_000L
        const val GARBAGE_BYTES = 40
    }
}
