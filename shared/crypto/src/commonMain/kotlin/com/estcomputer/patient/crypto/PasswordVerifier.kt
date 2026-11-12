package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.port.UnicodeNormalizer
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

internal sealed interface PasswordAttempt {
    /** The caller owns [mk] and zeroes it. [vault] is what is now stored, with the counter reset. */
    class Verified(val mk: SecretBytes, val vault: VaultFile) : PasswordAttempt {
        override fun toString(): String = "Verified(***)"
    }

    /** Verification ran and failed. [isWipeDue] is true when this failure reached the auto-wipe limit. */
    data class WrongPassword(val isWipeDue: Boolean) : PasswordAttempt

    data class LockedOut(val remainingMillis: Long) : PasswordAttempt

    /** Nothing was verified, and the failed-attempt counter was not changed. */
    data class Failed(val error: KeyError) : PasswordAttempt
}

/**
 * One password attempt against the vault. Callers hold the key manager's mutex.
 * The HW layer opens first because it needs no password, so a hardware failure never costs an attempt.
 */
internal class PasswordVerifier(
    private val keystore: KeystoreCipher,
    private val kdf: PasswordKdf,
    private val tools: KeyToolbox,
    private val normalizer: UnicodeNormalizer,
    private val context: VaultContext,
) {
    suspend fun verify(vault: VaultFile, password: SecretChars): PasswordAttempt {
        val armed = LockoutPolicy.rearmedAfterReboot(vault, context.bootInfo)
        val current = armed ?: vault
        val isPersisted = armed == null || context.store.write(armed) == VaultStoreResult.DONE
        val lockout = LockoutPolicy.status(current, context.bootInfo)
        return when {
            !isPersisted -> PasswordAttempt.Failed(KeyError.STORAGE_FAILED)
            lockout is LockoutStatus.Active -> PasswordAttempt.LockedOut(lockout.remaining.inWholeMilliseconds)
            else -> openHardwareLayer(current, password)
        }
    }

    private suspend fun openHardwareLayer(vault: VaultFile, password: SecretChars): PasswordAttempt =
        when (val opened = keystore.open(vault.hwKeyAlias, vault.wrappedMkPassword, CryptoConstants.AAD_MK_HW)) {
            is KeystoreOpenResult.Failed -> PasswordAttempt.Failed(opened.error.toKeyError())
            is KeystoreOpenResult.Ok ->
                try {
                    countAttempt(vault, opened.plaintext, password)
                } finally {
                    opened.plaintext.zero()
                }
        }

    // The counter is written before the slow work, so killing the app mid-attempt cannot skip it.
    private suspend fun countAttempt(vault: VaultFile, inner: SecretBytes, password: SecretChars): PasswordAttempt {
        val count = vault.failedAttempts + 1
        val attempted = vault.copy(
            failedAttempts = count,
            lockoutUntilElapsed = context.bootInfo.elapsedRealtimeMillis() +
                LockoutPolicy.delayFor(count).inWholeMilliseconds,
            lockoutBootCount = context.bootInfo.bootCount(),
        )
        val isCounted = try {
            context.store.write(attempted) == VaultStoreResult.DONE
        } catch (e: CancellationException) {
            giveBack(vault)
            throw e
        }
        return if (isCounted) {
            deriveAndCheck(vault, attempted, inner, password)
        } else {
            PasswordAttempt.Failed(KeyError.STORAGE_FAILED)
        }
    }

    private suspend fun deriveAndCheck(
        before: VaultFile,
        attempted: VaultFile,
        inner: SecretBytes,
        password: SecretChars,
    ): PasswordAttempt {
        val normalized = SecretChars.wrap(password.read { normalizer.normalizeNfkc(it) })
        try {
            val derived = try {
                kdf.derive(normalized, before.kdfParams)
            } catch (e: CancellationException) {
                // No verdict reached anyone, so the attempt is given back. A process kill keeps it.
                giveBack(before)
                throw e
            }
            return when (derived) {
                is KdfResult.Failed -> {
                    // Not a wrong password, so the attempt is given back.
                    context.store.write(before)
                    PasswordAttempt.Failed(derived.error)
                }
                is KdfResult.Derived ->
                    try {
                        checkPassword(attempted, inner, derived.key)
                    } finally {
                        derived.key.zero()
                    }
            }
        } finally {
            normalized.zero()
        }
    }

    private suspend fun checkPassword(attempted: VaultFile, inner: SecretBytes, pk: SecretBytes): PasswordAttempt =
        when (val opened = inner.read { tools.aead.open(pk, it, CryptoConstants.AAD_MK_PASSWORD) }) {
            // The wipe is decided only here, after a verification really failed.
            OpenResult.AuthFailed -> PasswordAttempt.WrongPassword(isWipeDue = isWipeDue(attempted))
            is OpenResult.Ok -> resetCounter(attempted, opened.plaintext)
        }

    private suspend fun resetCounter(attempted: VaultFile, mk: SecretBytes): PasswordAttempt {
        val reset = attempted.copy(
            failedAttempts = 0,
            lockoutUntilElapsed = 0,
            lockoutBootCount = context.bootInfo.bootCount(),
        )
        val isReset = try {
            context.store.write(reset) == VaultStoreResult.DONE
        } catch (e: CancellationException) {
            mk.zero()
            throw e
        }
        return if (isReset) {
            PasswordAttempt.Verified(mk, reset)
        } else {
            mk.zero()
            PasswordAttempt.Failed(KeyError.STORAGE_FAILED)
        }
    }

    private suspend fun giveBack(before: VaultFile) {
        withContext(NonCancellable) { context.store.write(before) }
    }

    private fun isWipeDue(vault: VaultFile): Boolean =
        vault.wipeAfterFailedAttempts > 0 && vault.failedAttempts >= vault.wipeAfterFailedAttempts
}
