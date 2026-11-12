package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import kotlinx.coroutines.sync.withLock

/** New salt, new calibration, same MK and same device key: nothing but the password layer is rewritten. */
internal class PasswordChangeFlow(
    private val verifier: PasswordVerifier,
    private val newPasswordDeriver: NewPasswordDeriver,
    private val hardware: HardwareWrap,
    private val tools: KeyToolbox,
    private val wipe: WipeRequester,
    private val context: VaultContext,
) {
    private sealed interface Outcome {
        class Done(val result: ChangePasswordResult) : Outcome

        data object WipeDue : Outcome
    }

    /** [old] may be null only within one minute of an unlock. The vault must be unlocked. */
    suspend fun change(old: SecretChars?, new: SecretChars): ChangePasswordResult =
        when (val outcome = context.mutex.withLock { attempt(old, new) }) {
            is Outcome.Done -> outcome.result
            // Outside the lock: the wipe destroys the keys through the key manager, which takes the same lock.
            Outcome.WipeDue -> {
                wipe.wipeAllData()
                ChangePasswordResult.Wiped
            }
        }

    private suspend fun attempt(old: SecretChars?, new: SecretChars): Outcome =
        if (context.isWipePending) {
            Outcome.Done(ChangePasswordResult.Wiped)
        } else {
            check(context.session.isUnlocked) { "vault is locked" }
            when (val read = context.store.read()) {
                is VaultReadResult.Ok -> authorize(read.vault, old, new)
                VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                    Outcome.Done(ChangePasswordResult.Failed(KeyError.VAULT_CORRUPT))
            }
        }

    private suspend fun authorize(vault: VaultFile, old: SecretChars?, new: SecretChars): Outcome =
        when {
            old != null -> verifyOld(vault, old, new)
            isRecentUnlock() -> rewrap(vault, new)
            // Without the old password a missing one is simply not the right one.
            else -> Outcome.Done(ChangePasswordResult.Failed(KeyError.WRONG_PASSWORD))
        }

    private fun isRecentUnlock(): Boolean {
        val unlockedAt = context.session.passwordUnlockElapsedMillis
        return unlockedAt != null && context.bootInfo.elapsedRealtimeMillis() - unlockedAt < RECENT_UNLOCK_MILLIS
    }

    private suspend fun verifyOld(vault: VaultFile, old: SecretChars, new: SecretChars): Outcome =
        when (val attempt = verifier.verify(vault, old)) {
            is PasswordAttempt.Verified ->
                try {
                    rewrap(attempt.vault, new)
                } finally {
                    attempt.mk.zero()
                }
            is PasswordAttempt.WrongPassword ->
                if (attempt.isWipeDue) {
                    context.isWipePending = true
                    Outcome.WipeDue
                } else {
                    Outcome.Done(ChangePasswordResult.Failed(KeyError.WRONG_PASSWORD))
                }
            is PasswordAttempt.LockedOut -> Outcome.Done(ChangePasswordResult.LockedOut(attempt.remainingMillis))
            is PasswordAttempt.Failed -> Outcome.Done(ChangePasswordResult.Failed(attempt.error))
        }

    private suspend fun rewrap(vault: VaultFile, new: SecretChars): Outcome =
        Outcome.Done(
            when (val derived = newPasswordDeriver.derive(new)) {
                is NewPasswordResult.Rejected -> ChangePasswordResult.PasswordRejected(derived.reason)
                is NewPasswordResult.Failed -> ChangePasswordResult.Failed(derived.error)
                is NewPasswordResult.Ready ->
                    try {
                        store(vault, derived)
                    } finally {
                        derived.key.zero()
                    }
            },
        )

    private suspend fun store(vault: VaultFile, derived: NewPasswordResult.Ready): ChangePasswordResult {
        val inner = tools.aead.seal(derived.key, context.session.requireMk(), CryptoConstants.AAD_MK_PASSWORD)
        return when (val wrapped = hardware.wrap(vault.hwKeyAlias, inner)) {
            is HardwareWrapResult.Failed -> ChangePasswordResult.Failed(wrapped.error)
            is HardwareWrapResult.Ok -> write(vault, derived.params, wrapped.blob)
        }
    }

    private suspend fun write(vault: VaultFile, params: KdfParams, wrapped: ByteArray): ChangePasswordResult {
        val updated = vault.copy(
            kdf = VaultFile.Kdf(vault.kdf.alg, params.salt, params.memoryKib, params.iterations, params.parallelism),
            wrappedMkPassword = wrapped,
        )
        return if (context.store.write(updated) == VaultStoreResult.DONE) {
            ChangePasswordResult.Changed
        } else {
            ChangePasswordResult.Failed(KeyError.STORAGE_FAILED)
        }
    }

    private companion object {
        const val RECENT_UNLOCK_MILLIS = 60_000L
    }
}
