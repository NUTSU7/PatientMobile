package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.WipeSettingResult
import kotlinx.coroutines.sync.withLock

/**
 * Turns the auto-wipe on or off. The patient proves the password, or unlocked with it less than a minute ago,
 * so a biometric unlock alone can neither arm nor disarm the wipe.
 */
internal class WipeSettingFlow(
    private val verifier: PasswordVerifier,
    private val wipe: WipeRequester,
    private val context: VaultContext,
) {
    private sealed interface Outcome {
        class Done(val result: WipeSettingResult) : Outcome

        data object WipeDue : Outcome
    }

    suspend fun set(attempts: Int?, password: SecretChars?): WipeSettingResult {
        require(attempts == null || attempts in CryptoConstants.WIPE_AFTER_MIN..CryptoConstants.WIPE_AFTER_MAX) {
            "attempts out of range"
        }
        return when (val outcome = context.mutex.withLock { attempt(attempts, password) }) {
            is Outcome.Done -> outcome.result
            // Outside the lock: the wipe destroys the keys through the key manager, which takes the same lock.
            Outcome.WipeDue -> {
                wipe.wipeAllData()
                WipeSettingResult.WIPED
            }
        }
    }

    private suspend fun attempt(attempts: Int?, password: SecretChars?): Outcome =
        if (context.isWipePending) {
            Outcome.Done(WipeSettingResult.WIPED)
        } else {
            check(context.session.isUnlocked) { "vault is locked" }
            when (val read = context.store.read()) {
                is VaultReadResult.Ok -> authorize(read.vault, attempts, password)
                VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                    Outcome.Done(WipeSettingResult.STORAGE_FAILED)
            }
        }

    private suspend fun authorize(vault: VaultFile, attempts: Int?, password: SecretChars?): Outcome =
        when {
            password != null -> verify(vault, attempts, password)
            context.hasRecentPasswordUnlock() -> Outcome.Done(save(vault, attempts))
            else -> Outcome.Done(WipeSettingResult.PASSWORD_REQUIRED)
        }

    private suspend fun verify(vault: VaultFile, attempts: Int?, password: SecretChars): Outcome =
        when (val attempt = verifier.verify(vault, password)) {
            is PasswordAttempt.Verified ->
                try {
                    Outcome.Done(save(attempt.vault, attempts))
                } finally {
                    attempt.mk.zero()
                }
            is PasswordAttempt.WrongPassword ->
                if (attempt.isWipeDue) {
                    context.isWipePending = true
                    Outcome.WipeDue
                } else {
                    Outcome.Done(WipeSettingResult.WRONG_PASSWORD)
                }
            is PasswordAttempt.LockedOut -> Outcome.Done(WipeSettingResult.LOCKED_OUT)
            is PasswordAttempt.Failed -> Outcome.Done(WipeSettingResult.STORAGE_FAILED)
        }

    private suspend fun save(vault: VaultFile, attempts: Int?): WipeSettingResult {
        val written = context.store.write(vault.copy(wipeAfterFailedAttempts = attempts ?: WIPE_DISABLED))
        return if (written == VaultStoreResult.DONE) WipeSettingResult.SAVED else WipeSettingResult.STORAGE_FAILED
    }

    private companion object {
        const val WIPE_DISABLED = 0
    }
}
