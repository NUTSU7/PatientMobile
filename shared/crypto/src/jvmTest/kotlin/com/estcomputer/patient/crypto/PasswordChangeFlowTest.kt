package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.PasswordRejection
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.UnlockResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs

class PasswordChangeFlowTest {
    private fun chars(text: String) = SecretChars.wrap(text.toCharArray())

    private suspend fun unlockedRig(scope: TestScope): VaultRig {
        val rig = VaultRig(scope)
        assertIs<SetupResult.Ready>(rig.setup.prepare(chars(PASSWORD))).pending.commit()
        return rig
    }

    private suspend fun VaultRig.vault() = assertIs<VaultReadResult.Ok>(store.read()).vault

    private suspend fun VaultRig.canUnlockWith(password: String): Boolean {
        val mk = session.requireMk().read { it.copyOf() }
        session.clear()
        boot.elapsedMillis += SKIP
        val result = unlock.unlock(chars(password))
        val isSame = result is UnlockResult.Unlocked && session.requireMk().read { it.contentEquals(mk) }
        session.clear()
        return isSame
    }

    @Test
    fun change_withOldPassword_rewrapsAndKeepsMk() = runTest {
        val rig = unlockedRig(this)
        val before = rig.vault()

        assertEquals(ChangePasswordResult.Changed, rig.change.change(chars(PASSWORD), chars(NEW_PASSWORD)))

        val after = rig.vault()
        assertFalse(before.kdf.salt.contentEquals(after.kdf.salt))
        assertContentEquals(before.wrappedMkRecovery, after.wrappedMkRecovery)
        assertEquals(before.hwKeyAlias, after.hwKeyAlias)
        assertEquals(1, rig.keystore.generateCalls)
        assertEquals(true, rig.canUnlockWith(NEW_PASSWORD))
    }

    @Test
    fun change_oldPasswordNoLongerWorks() = runTest {
        val rig = unlockedRig(this)
        rig.change.change(chars(PASSWORD), chars(NEW_PASSWORD))

        assertEquals(false, rig.canUnlockWith(PASSWORD))
    }

    @Test
    fun change_wrongOldPassword_failsAndLeavesTheVault() = runTest {
        val rig = unlockedRig(this)

        assertEquals(
            ChangePasswordResult.Failed(KeyError.WRONG_PASSWORD),
            rig.change.change(chars("not the password"), chars(NEW_PASSWORD)),
        )
        assertEquals(1, rig.vault().failedAttempts)
        assertEquals(true, rig.canUnlockWith(PASSWORD))
    }

    @Test
    fun change_withoutOldPasswordWithinAMinute_isAllowed() = runTest {
        val rig = unlockedRig(this)
        rig.boot.elapsedMillis += 59_000

        assertEquals(ChangePasswordResult.Changed, rig.change.change(null, chars(NEW_PASSWORD)))
        assertEquals(true, rig.canUnlockWith(NEW_PASSWORD))
    }

    @Test
    fun change_withoutOldPasswordAfter61Seconds_isRejected() = runTest {
        val rig = unlockedRig(this)
        val before = rig.files.vaultBytes!!.copyOf()
        rig.boot.elapsedMillis += 61_000

        assertEquals(
            ChangePasswordResult.Failed(KeyError.WRONG_PASSWORD),
            rig.change.change(null, chars(NEW_PASSWORD)),
        )
        assertContentEquals(before, rig.files.vaultBytes)
    }

    @Test
    fun change_afterBiometricUnlock_needsTheOldPassword() = runTest {
        val rig = unlockedRig(this)
        val challenge = assertIs<BiometricBeginResult.Ready>(rig.biometrics.beginBiometricEnrolment()).challenge
        rig.biometrics.completeBiometricEnrolment(challenge)
        rig.session.clear()
        val unlockChallenge = assertIs<BiometricBeginResult.Ready>(rig.biometrics.beginBiometricUnlock()).challenge
        assertIs<UnlockResult.Unlocked>(rig.biometrics.completeBiometricUnlock(unlockChallenge))
        val before = rig.files.vaultBytes!!.copyOf()

        assertEquals(
            ChangePasswordResult.Failed(KeyError.WRONG_PASSWORD),
            rig.change.change(null, chars(NEW_PASSWORD)),
        )
        assertContentEquals(before, rig.files.vaultBytes)
        assertEquals(ChangePasswordResult.Changed, rig.change.change(chars(PASSWORD), chars(NEW_PASSWORD)))
    }

    @Test
    fun change_weakNewPassword_isRejectedAndNothingChanges() = runTest {
        val rig = unlockedRig(this)
        val before = rig.files.vaultBytes!!.copyOf()

        assertEquals(
            ChangePasswordResult.PasswordRejected(PasswordRejection.TOO_SHORT),
            rig.change.change(null, chars("short")),
        )
        assertContentEquals(before, rig.files.vaultBytes)
    }

    @Test
    fun change_writeFails_oldPasswordStillWorks() = runTest {
        val rig = unlockedRig(this)
        rig.files.failingStep = "temp"

        assertEquals(
            ChangePasswordResult.Failed(KeyError.STORAGE_FAILED),
            rig.change.change(null, chars(NEW_PASSWORD)),
        )
        rig.files.failingStep = null
        assertEquals(true, rig.canUnlockWith(PASSWORD))
    }

    @Test
    fun change_hardwareSealFails_oldPasswordStillWorks() = runTest {
        val rig = unlockedRig(this)
        rig.keystore.failNextSeal = KeystoreError.HW_KEY_INVALIDATED

        assertEquals(
            ChangePasswordResult.Failed(KeyError.HW_KEY_INVALIDATED),
            rig.change.change(null, chars(NEW_PASSWORD)),
        )
        assertEquals(true, rig.canUnlockWith(PASSWORD))
    }

    @Test
    fun change_oldPasswordAtWipeLimit_wipes() = runTest {
        val rig = unlockedRig(this)
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))

        assertEquals(ChangePasswordResult.Wiped, rig.change.change(chars("not the password"), chars(NEW_PASSWORD)))

        assertEquals(1, rig.wipe.calls)
    }

    @Test
    fun change_whileLocked_isACallerBug() = runTest {
        val rig = unlockedRig(this)
        rig.session.clear()

        assertFailsWith<IllegalStateException> { rig.change.change(chars(PASSWORD), chars(NEW_PASSWORD)) }
    }

    @Test
    fun change_keepsBiometricBlob() = runTest {
        val rig = unlockedRig(this)
        val bio = ByteArray(CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.KEY_SIZE_BYTES + 40)
        rig.store.write(rig.vault().copy(wrappedMkBiometric = bio, bioKeyAlias = CryptoConstants.BIO_KEY_ALIAS))

        rig.change.change(null, chars(NEW_PASSWORD))

        assertContentEquals(bio, rig.vault().wrappedMkBiometric)
    }

    private companion object {
        const val PASSWORD = "correct horse battery"
        const val NEW_PASSWORD = "another long passphrase"
        const val SKIP = 4_000_000L
    }
}
