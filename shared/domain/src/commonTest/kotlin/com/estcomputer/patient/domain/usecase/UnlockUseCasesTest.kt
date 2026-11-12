package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.BiometricEnrolmentResult
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.model.PendingSetup
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupCommitResult
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BiometricChallenge
import com.estcomputer.patient.domain.port.BiometricKeyManager
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.session.FakeKeyManager
import com.estcomputer.patient.domain.session.SessionCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UnlockUseCasesTest {

    private val events = mutableListOf<String>()
    private val keys = FakeKeyManager(events)
    private var unlockResult: UnlockResult = UnlockResult.Unlocked(needsBiometricRefresh = false)
    private var commitResult: SetupCommitResult = SetupCommitResult.Committed

    /** Runs while the key manager is unlocking, e.g. a screen-off lock. */
    private var duringUnlock: suspend () -> Unit = {}

    private fun TestScope.newCoordinator(): SessionCoordinator {
        val appScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        return SessionCoordinator(appScope, keys, emptyList())
    }

    private val keyManager = object : KeyManager by keys {
        override suspend fun unlockWithPassword(password: SecretChars): UnlockResult {
            duringUnlock()
            return unlockResult
        }

        override suspend fun unlockWithRecovery(words: SecretChars, newPassword: SecretChars): UnlockResult {
            duringUnlock()
            return unlockResult
        }
    }

    private val biometrics = object : BiometricKeyManager {
        override suspend fun beginBiometricUnlock(): BiometricBeginResult = error("unused")
        override suspend fun completeBiometricUnlock(challenge: BiometricChallenge): UnlockResult {
            duringUnlock()
            return unlockResult
        }
        override suspend fun beginBiometricEnrolment(): BiometricBeginResult = error("unused")
        override suspend fun completeBiometricEnrolment(challenge: BiometricChallenge): BiometricEnrolmentResult =
            error("unused")
        override suspend fun disableBiometrics() = error("unused")
    }

    private val pending = object : PendingSetup {
        override val recoveryWords: SecretChars = SecretChars.copyOf(CharArray(0))
        override suspend fun commit(): SetupCommitResult {
            duringUnlock()
            return commitResult
        }
        override fun abandon() = Unit
    }

    private val challenge = object : BiometricChallenge {}

    private fun password() = SecretChars.copyOf("password".toCharArray())

    @Test
    fun unlockWithPassword_unlocked_opensSession() = runTest {
        val coordinator = newCoordinator()

        val result = UnlockWithPassword(keyManager, coordinator)(password())

        assertEquals(UnlockResult.Unlocked(needsBiometricRefresh = false), result)
        assertEquals(LockState.Unlocked, coordinator.lockState.value)
        assertNotNull(coordinator.unlockedSessionScope)
    }

    @Test
    fun unlockWithPassword_wrongPassword_opensNoSession() = runTest {
        val coordinator = newCoordinator()
        unlockResult = UnlockResult.Failed(KeyError.WRONG_PASSWORD)

        val result = UnlockWithPassword(keyManager, coordinator)(password())

        assertEquals(UnlockResult.Failed(KeyError.WRONG_PASSWORD), result)
        assertNull(coordinator.unlockedSessionScope)
    }

    @Test
    fun unlockWithPassword_lockedWhileUnlocking_zeroesKeysAndOpensNoSession() = runTest {
        val coordinator = newCoordinator()
        duringUnlock = { coordinator.lock() }

        val result = UnlockWithPassword(keyManager, coordinator)(password())

        assertEquals(UnlockResult.Failed(KeyError.LOCKED_DURING_UNLOCK), result)
        assertEquals(listOf("keys.lock", "keys.lock"), events)
        assertNull(coordinator.unlockedSessionScope)
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
    }

    @Test
    fun unlockWithRecovery_unlocked_opensSession() = runTest {
        val coordinator = newCoordinator()

        UnlockWithRecovery(keyManager, coordinator)(password(), password())

        assertEquals(LockState.Unlocked, coordinator.lockState.value)
    }

    @Test
    fun unlockWithRecovery_lockedWhileUnlocking_opensNoSession() = runTest {
        val coordinator = newCoordinator()
        duringUnlock = { coordinator.lock() }

        val result = UnlockWithRecovery(keyManager, coordinator)(password(), password())

        assertEquals(UnlockResult.Failed(KeyError.LOCKED_DURING_UNLOCK), result)
        assertNull(coordinator.unlockedSessionScope)
    }

    @Test
    fun completeBiometricUnlock_unlocked_opensSession() = runTest {
        val coordinator = newCoordinator()

        CompleteBiometricUnlock(biometrics, coordinator)(challenge)

        assertEquals(LockState.Unlocked, coordinator.lockState.value)
    }

    @Test
    fun completeBiometricUnlock_policyExpired_opensNoSession() = runTest {
        val coordinator = newCoordinator()
        unlockResult = UnlockResult.Failed(KeyError.BIOMETRIC_POLICY_EXPIRED)

        CompleteBiometricUnlock(biometrics, coordinator)(challenge)

        assertNull(coordinator.unlockedSessionScope)
    }

    @Test
    fun commitSetup_committed_opensSession() = runTest {
        val coordinator = newCoordinator()
        coordinator.setIdleState(LockState.Unconfigured)

        val result = CommitSetup(coordinator)(pending)

        assertEquals(SetupCommitResult.Committed, result)
        assertEquals(LockState.Unlocked, coordinator.lockState.value)
    }

    @Test
    fun commitSetup_lockedWhileCommitting_showsLockedNotUnconfigured() = runTest {
        val coordinator = newCoordinator()
        coordinator.setIdleState(LockState.Unconfigured)
        duringUnlock = { coordinator.lock() }

        val result = CommitSetup(coordinator)(pending)

        assertEquals(SetupCommitResult.Failed(KeyError.LOCKED_DURING_UNLOCK), result)
        assertEquals(LockState.Locked(isRecoveryRequired = false), coordinator.lockState.value)
    }

    @Test
    fun commitSetup_failed_opensNoSession() = runTest {
        val coordinator = newCoordinator()
        commitResult = SetupCommitResult.Failed(KeyError.STORAGE_FAILED)

        CommitSetup(coordinator)(pending)

        assertNull(coordinator.unlockedSessionScope)
    }
}
