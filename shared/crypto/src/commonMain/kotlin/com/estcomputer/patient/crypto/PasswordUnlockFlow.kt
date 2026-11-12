package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.UnlockResult
import kotlinx.coroutines.sync.withLock

internal class PasswordUnlockFlow(
    private val verifier: PasswordVerifier,
    private val wipe: WipeRequester,
    private val context: VaultContext,
) {
    private sealed interface Outcome {
        class Done(val result: UnlockResult) : Outcome

        data object WipeDue : Outcome
    }

    suspend fun unlock(password: SecretChars): UnlockResult =
        when (val outcome = context.mutex.withLock { attempt(password) }) {
            is Outcome.Done -> outcome.result
            // Outside the lock: the wipe destroys the keys through the key manager, which takes the same lock.
            Outcome.WipeDue -> {
                wipe.wipeAllData()
                UnlockResult.Wiped
            }
        }

    private suspend fun attempt(password: SecretChars): Outcome =
        when (val read = context.store.read()) {
            is VaultReadResult.Ok -> outcomeOf(read.vault, verifier.verify(read.vault, password))
            VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                Outcome.Done(UnlockResult.Failed(KeyError.VAULT_CORRUPT))
        }

    private fun outcomeOf(vault: VaultFile, attempt: PasswordAttempt): Outcome =
        when (attempt) {
            is PasswordAttempt.Verified -> {
                context.session.start(attempt.mk, context.clock.now(), context.bootInfo.elapsedRealtimeMillis())
                Outcome.Done(UnlockResult.Unlocked(needsBiometricRefresh = vault.wrappedMkBiometric != null))
            }
            is PasswordAttempt.WrongPassword ->
                if (attempt.isWipeDue) Outcome.WipeDue else Outcome.Done(UnlockResult.Failed(KeyError.WRONG_PASSWORD))
            is PasswordAttempt.LockedOut -> Outcome.Done(UnlockResult.LockedOut(attempt.remainingMillis))
            is PasswordAttempt.Failed -> Outcome.Done(UnlockResult.Failed(attempt.error))
        }
}
