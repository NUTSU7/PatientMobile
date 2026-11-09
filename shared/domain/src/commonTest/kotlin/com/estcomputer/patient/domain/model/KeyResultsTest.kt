package com.estcomputer.patient.domain.model

import com.estcomputer.patient.domain.port.BiometricChallenge
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyResultsTest {

    private val challenge = object : BiometricChallenge {
        override fun toString(): String = "cipher-state"
    }

    private val pending = object : PendingSetup {
        override val recoveryWords: SecretChars = SecretChars.copyOf("word".toCharArray())
        override suspend fun commit(): SetupCommitResult = SetupCommitResult.Committed
        override fun abandon() = Unit
    }

    @Test
    fun unlockResult_whenExhaustive_compilesWithoutElse() {
        val results = listOf(
            UnlockResult.Unlocked(needsBiometricRefresh = false),
            UnlockResult.Failed(KeyError.WRONG_PASSWORD),
            UnlockResult.PasswordRejected(PasswordRejection.COMMON),
            UnlockResult.LockedOut(remainingMillis = 30_000),
            UnlockResult.Wiped,
        )

        val labels = results.map { result ->
            when (result) {
                is UnlockResult.Unlocked -> "unlocked"
                is UnlockResult.Failed -> "failed"
                is UnlockResult.PasswordRejected -> "rejected"
                is UnlockResult.LockedOut -> "lockedOut"
                UnlockResult.Wiped -> "wiped"
            }
        }

        assertEquals(5, labels.size)
    }

    @Test
    fun setupResult_whenExhaustive_compilesWithoutElse() {
        val results = listOf(
            SetupResult.Ready(pending),
            SetupResult.PasswordRejected(PasswordRejection.TOO_SHORT),
            SetupResult.Failed(KeyError.OUT_OF_MEMORY),
        )

        val labels = results.map { result ->
            when (result) {
                is SetupResult.Ready -> "ready"
                is SetupResult.PasswordRejected -> "rejected"
                is SetupResult.Failed -> "failed"
            }
        }

        assertEquals(3, labels.size)
    }

    @Test
    fun changePasswordResult_whenExhaustive_compilesWithoutElse() {
        val results = listOf(
            ChangePasswordResult.Changed,
            ChangePasswordResult.PasswordRejected(PasswordRejection.TOO_LONG),
            ChangePasswordResult.Failed(KeyError.VAULT_CORRUPT),
            ChangePasswordResult.LockedOut(remainingMillis = 1),
        )

        val labels = results.map { result ->
            when (result) {
                ChangePasswordResult.Changed -> "changed"
                is ChangePasswordResult.PasswordRejected -> "rejected"
                is ChangePasswordResult.Failed -> "failed"
                is ChangePasswordResult.LockedOut -> "lockedOut"
            }
        }

        assertEquals(4, labels.size)
    }

    @Test
    fun biometricResults_whenExhaustive_compileWithoutElse() {
        val begin: BiometricBeginResult = BiometricBeginResult.Ready(challenge)
        val enrolment: BiometricEnrolmentResult = BiometricEnrolmentResult.Enabled
        val commit: SetupCommitResult = SetupCommitResult.Committed

        val labels = listOf(
            when (begin) {
                is BiometricBeginResult.Ready -> "ready"
                is BiometricBeginResult.Failed -> "failed"
            },
            when (enrolment) {
                BiometricEnrolmentResult.Enabled -> "enabled"
                is BiometricEnrolmentResult.Failed -> "failed"
            },
            when (commit) {
                SetupCommitResult.Committed -> "committed"
                is SetupCommitResult.Failed -> "failed"
            },
        )

        assertEquals(listOf("ready", "enabled", "committed"), labels)
    }

    @Test
    fun toString_forResultsHoldingSecrets_isRedacted() {
        assertEquals("Ready", SetupResult.Ready(pending).toString())
        assertEquals("Ready", BiometricBeginResult.Ready(challenge).toString())
        assertEquals("***", pending.recoveryWords.toString())
    }

    @Test
    fun toString_forFailureResults_holdsOnlyTheCode() {
        assertEquals("Failed(error=WRONG_PASSWORD)", UnlockResult.Failed(KeyError.WRONG_PASSWORD).toString())
        assertEquals("LockedOut(remainingMillis=30000)", UnlockResult.LockedOut(30_000).toString())
    }
}
