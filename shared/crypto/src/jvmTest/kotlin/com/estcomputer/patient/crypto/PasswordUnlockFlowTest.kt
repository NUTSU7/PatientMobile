package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.UnlockResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PasswordUnlockFlowTest {
    private fun chars(text: String) = SecretChars.wrap(text.toCharArray())

    private suspend fun VaultRig.setUpAndLock(password: String = PASSWORD): VaultRig {
        val pending = assertIs<SetupResult.Ready>(
            setup.prepare(chars(password)),
        ).pending
        pending.commit()
        session.clear()
        return this
    }

    private suspend fun VaultRig.vault() = assertIs<VaultReadResult.Ok>(store.read()).vault

    private suspend fun VaultRig.failedAttempts() = vault().failedAttempts

    private fun TestScope.rig() = VaultRig(this)

    private suspend fun VaultRig.wrongAttempts(count: Int) {
        repeat(count) {
            // Skip any lockout the earlier failures armed.
            boot.elapsedMillis += LOCKOUT_SKIP
            assertEquals(UnlockResult.Failed(KeyError.WRONG_PASSWORD), unlock.unlock(chars("wrong password")))
        }
    }

    @Test
    fun unlock_correctPassword_unlocks() = runTest {
        val rig = rig().setUpAndLock()

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), rig.unlock.unlock(chars(PASSWORD)))

        assertTrue(rig.session.isUnlocked)
    }

    @Test
    fun unlock_wrongPassword_failsAndCounts() = runTest {
        val rig = rig().setUpAndLock()

        assertEquals(UnlockResult.Failed(KeyError.WRONG_PASSWORD), rig.unlock.unlock(chars("wrong password")))

        assertEquals(1, rig.failedAttempts())
        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun unlock_success_resetsCounterAndLockout() = runTest {
        val rig = rig().setUpAndLock()
        rig.wrongAttempts(5)
        rig.boot.elapsedMillis += LOCKOUT_SKIP

        rig.unlock.unlock(chars(PASSWORD))

        val vault = rig.vault()
        assertEquals(0, vault.failedAttempts)
        assertEquals(0, vault.lockoutUntilElapsed)
    }

    @Test
    fun unlock_activeLockout_isReportedWithoutWork() = runTest {
        val rig = rig().setUpAndLock()
        rig.wrongAttempts(5)
        val derivesBefore = rig.kdf.deriveCalls

        val result = rig.unlock.unlock(chars(PASSWORD))

        assertIs<UnlockResult.LockedOut>(result)
        assertEquals(derivesBefore, rig.kdf.deriveCalls)
        assertEquals(5, rig.failedAttempts())
    }

    @Test
    fun unlock_hardwareKeyMissing_neverCountsAndNeverRunsKdf() = runTest {
        val rig = rig().setUpAndLock()
        rig.keystore.keys.clear()

        assertEquals(UnlockResult.Failed(KeyError.RECOVERY_REQUIRED), rig.unlock.unlock(chars(PASSWORD)))

        assertEquals(0, rig.kdf.deriveCalls)
        assertEquals(0, rig.failedAttempts())
    }

    @Test
    fun unlock_hardwareKeyInvalidatedOrDeviceLocked_neverCounts() = runTest {
        val rig = rig().setUpAndLock()
        rig.keystore.openError = KeystoreError.HW_KEY_INVALIDATED
        assertEquals(UnlockResult.Failed(KeyError.HW_KEY_INVALIDATED), rig.unlock.unlock(chars(PASSWORD)))
        rig.keystore.openError = KeystoreError.DEVICE_LOCKED
        assertEquals(UnlockResult.Failed(KeyError.DEVICE_LOCKED), rig.unlock.unlock(chars(PASSWORD)))

        assertEquals(0, rig.kdf.deriveCalls)
        assertEquals(0, rig.failedAttempts())
    }

    @Test
    fun unlock_hardwareKeyReplaced_isRecoveryRequiredNotWrongPassword() = runTest {
        val rig = rig().setUpAndLock()
        rig.keystore.keys[CryptoConstants.HW_KEY_ALIAS] = ByteArray(CryptoConstants.KEY_SIZE_BYTES) { 9 }

        assertEquals(UnlockResult.Failed(KeyError.RECOVERY_REQUIRED), rig.unlock.unlock(chars(PASSWORD)))
        assertEquals(0, rig.failedAttempts())
    }

    @Test
    fun unlock_counterSurvivesAKillDuringTheKdf() = runTest {
        val rig = rig().setUpAndLock()
        rig.kdf.crash = IllegalStateException("killed")

        assertFailsWith<IllegalStateException> { rig.unlock.unlock(chars(PASSWORD)) }

        assertEquals(1, rig.failedAttempts())
    }

    @Test
    fun unlock_whenCancelledDuringKdf_restoresCounter() = runTest {
        val rig = rig().setUpAndLock()
        rig.wrongAttempts(2)
        rig.boot.elapsedMillis += LOCKOUT_SKIP
        val before = rig.vault()
        rig.kdf.onDerive = { awaitCancellation() }

        val job = launch { rig.unlock.unlock(chars(PASSWORD)) }
        testScheduler.runCurrent()
        assertEquals(3, rig.failedAttempts())
        job.cancel()
        job.join()

        val after = rig.vault()
        assertEquals(2, after.failedAttempts)
        assertEquals(before.lockoutUntilElapsed, after.lockoutUntilElapsed)
    }

    @Test
    fun unlock_whenCancelledDuringKdf_rethrowsCancellation() = runTest {
        val rig = rig().setUpAndLock()
        rig.kdf.onDerive = { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> { rig.unlock.unlock(chars(PASSWORD)) }

        assertEquals(0, rig.failedAttempts())
        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun unlock_whenCancelledDuringCounterWrite_restoresCounter() = runTest {
        val rig = rig().setUpAndLock()
        val writesBefore = rig.files.writes
        val job = launch {
            rig.files.onReplace = { count -> if (count == writesBefore + 1) coroutineContext.cancelJob() }
            rig.unlock.unlock(chars(PASSWORD))
        }
        job.join()

        assertTrue(job.isCancelled)
        assertEquals(0, rig.failedAttempts())
        assertEquals(0, rig.kdf.deriveCalls)
    }

    @Test
    fun unlock_whenCancelledDuringResetWrite_zeroesMasterKey() = runTest {
        val rig = rig().setUpAndLock()
        val writesBefore = rig.files.writes
        val job = launch {
            // The second write is the counter reset after a verified password.
            rig.files.onReplace = { count -> if (count == writesBefore + 2) coroutineContext.cancelJob() }
            rig.unlock.unlock(chars(PASSWORD))
        }
        job.join()

        assertTrue(job.isCancelled)
        assertTrue(requireNotNull(rig.aead.lastOpened).isZeroed)
        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun unlock_killAtLimitThenCorrectPassword_unlocksWithoutWipe() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))
        rig.kdf.crash = IllegalStateException("killed")
        assertFailsWith<IllegalStateException> { rig.unlock.unlock(chars(PASSWORD)) }
        assertEquals(10, rig.failedAttempts())
        rig.kdf.crash = null
        rig.boot.elapsedMillis += LOCKOUT_SKIP

        val result = rig.unlock.unlock(chars(PASSWORD))

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), result)
        assertEquals(0, rig.wipe.calls)
    }

    @Test
    fun unlock_verifiedFailureAtLimit_wipes() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))

        assertEquals(UnlockResult.Wiped, rig.unlock.unlock(chars("wrong password")))

        assertEquals(1, rig.wipe.calls)
    }

    @Test
    fun unlock_verifiedFailureBelowLimit_doesNotWipe() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 8))

        assertEquals(UnlockResult.Failed(KeyError.WRONG_PASSWORD), rig.unlock.unlock(chars("wrong password")))

        assertEquals(0, rig.wipe.calls)
    }

    @Test
    fun unlock_wipeDisabled_neverWipes() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(failedAttempts = 30))

        rig.boot.elapsedMillis += LOCKOUT_SKIP
        rig.unlock.unlock(chars("wrong password"))

        assertEquals(0, rig.wipe.calls)
    }

    @Test
    fun unlock_wipeRunsOutsideTheLock() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))
        var reachedLock = false
        // The real wipe calls back into the key manager; a held lock would deadlock here.
        rig.wipe.onWipe = { rig.mutex.withLock { reachedLock = true } }

        rig.unlock.unlock(chars("wrong password"))

        assertTrue(reachedLock)
    }

    @Test
    fun unlock_afterReboot_rearmsTheFullDelay() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(failedAttempts = 5, lockoutUntilElapsed = 0, lockoutBootCount = 1))
        rig.boot.bootCount = 2
        rig.boot.elapsedMillis = 5_000

        val result = rig.unlock.unlock(chars(PASSWORD))

        assertEquals(UnlockResult.LockedOut(CryptoConstants.LOCKOUT_SCHEDULE.getValue(5).inWholeMilliseconds), result)
        assertEquals(2, rig.vault().lockoutBootCount)
    }

    @Test
    fun unlock_kdfOutOfMemory_givesTheAttemptBack() = runTest {
        val rig = rig().setUpAndLock()
        rig.kdf.failure = KeyError.OUT_OF_MEMORY

        assertEquals(UnlockResult.Failed(KeyError.OUT_OF_MEMORY), rig.unlock.unlock(chars(PASSWORD)))

        assertEquals(0, rig.failedAttempts())
    }

    @Test
    fun unlock_counterWriteFails_failsWithoutRunningTheKdf() = runTest {
        val rig = rig().setUpAndLock()
        rig.files.failingStep = "temp"

        assertEquals(UnlockResult.Failed(KeyError.STORAGE_FAILED), rig.unlock.unlock(chars(PASSWORD)))

        assertEquals(0, rig.kdf.deriveCalls)
    }

    @Test
    fun unlock_noVaultOrCorruptVault_isVaultCorrupt() = runTest {
        val rig = rig()
        assertEquals(UnlockResult.Failed(KeyError.VAULT_CORRUPT), rig.unlock.unlock(chars(PASSWORD)))

        rig.files.vaultBytes = "{}".encodeToByteArray()
        assertEquals(UnlockResult.Failed(KeyError.VAULT_CORRUPT), rig.unlock.unlock(chars(PASSWORD)))
    }

    @Test
    fun unlock_biometricsEnabled_asksForARefresh() = runTest {
        val rig = rig().setUpAndLock()
        val bio = ByteArray(CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.KEY_SIZE_BYTES + 40)
        rig.store.write(rig.vault().copy(wrappedMkBiometric = bio, bioKeyAlias = CryptoConstants.BIO_KEY_ALIAS))

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = true), rig.unlock.unlock(chars(PASSWORD)))
    }

    @Test
    fun unlock_passwordChangedByNfkc_matchesSetupNormalisation() = runTest {
        val rig = rig().setUpAndLock(password = "ｐａｓｓｗｏｒｄ１２３４")

        assertEquals(
            UnlockResult.Unlocked(needsBiometricRefresh = false),
            rig.unlock.unlock(chars("password1234")),
        )
    }

    @Test
    fun unlock_twoConcurrentCalls_runOneAtATime() = runTest {
        val rig = rig().setUpAndLock()
        var active = 0
        var maxActive = 0
        rig.kdf.onDerive = {
            active++
            maxActive = maxOf(maxActive, active)
            yield()
            active--
        }

        val results = listOf(
            async { rig.unlock.unlock(chars(PASSWORD)) },
            async { rig.unlock.unlock(chars(PASSWORD)) },
        ).awaitAll()

        assertEquals(1, maxActive)
        assertTrue(results.all { it is UnlockResult.Unlocked })
    }

    @Test
    fun unlock_queuedAttemptsBehindTheWipingOne_areRefusedAndWipeRunsOnce() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))
        rig.kdf.onDerive = { yield() }

        val results = listOf(
            async { rig.unlock.unlock(chars("wrong password")) },
            async { rig.unlock.unlock(chars(PASSWORD)) },
            async { rig.unlock.unlock(chars("wrong again")) },
        ).awaitAll()

        assertEquals(listOf(UnlockResult.Wiped, UnlockResult.Wiped, UnlockResult.Wiped), results)
        assertEquals(1, rig.wipe.calls)
        assertFalse(rig.session.isUnlocked)
    }

    @Test
    fun unlock_afterANewSetup_worksAgain() = runTest {
        val rig = rig().setUpAndLock()
        rig.store.write(rig.vault().copy(wipeAfterFailedAttempts = 10, failedAttempts = 9))
        rig.unlock.unlock(chars("wrong password"))
        rig.files.vaultBytes = null

        rig.setUpAndLock()

        assertIs<UnlockResult.Unlocked>(rig.unlock.unlock(chars(PASSWORD)))
    }

    private companion object {
        const val PASSWORD = "correct horse battery"
        const val LOCKOUT_SKIP = 4_000_000L
    }
}

private fun CoroutineContext.cancelJob() {
    requireNotNull(this[Job]).cancel()
}
