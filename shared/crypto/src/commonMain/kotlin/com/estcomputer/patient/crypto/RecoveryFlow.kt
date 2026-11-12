package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.UnlockResult
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

/**
 * Forgotten-password path (D12): the recovery words open the vault and set a new password.
 * MK, the recovery blob and the export key stay as they were.
 */
internal class RecoveryFlow(
    private val newPasswordDeriver: NewPasswordDeriver,
    private val hardware: HardwareWrap,
    private val biometricKeystore: BiometricKeystore,
    private val tools: KeyToolbox,
    private val context: VaultContext,
) {
    // In memory only: never written to the vault and never counted toward the auto-wipe, so a typo in the
    // words can't delete data. The words carry 256 random bits, so guessing is infeasible anyway.
    private var failures = 0
    private var blockedUntilElapsed = 0L

    suspend fun unlock(words: SecretChars, newPassword: SecretChars): UnlockResult =
        context.mutex.withLock {
            if (context.isWipePending) {
                UnlockResult.Wiped
            } else {
                when (val read = context.store.read()) {
                    is VaultReadResult.Ok -> rateLimited(read.vault, words, newPassword)
                    VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                        UnlockResult.Failed(KeyError.VAULT_CORRUPT)
                }
            }
        }

    private suspend fun rateLimited(vault: VaultFile, words: SecretChars, newPassword: SecretChars): UnlockResult {
        val remaining = blockedUntilElapsed - context.bootInfo.elapsedRealtimeMillis()
        return if (remaining > 0) UnlockResult.LockedOut(remaining) else decode(vault, words, newPassword)
    }

    // A bad checksum or an unknown word is rejected here, before any key work or rate-limit cost.
    private suspend fun decode(vault: VaultFile, words: SecretChars, newPassword: SecretChars): UnlockResult =
        when (val decoded = tools.bip39.decode(words)) {
            is DecodeResult.Ok ->
                try {
                    openRecoveryBlob(vault, decoded.entropy, newPassword)
                } finally {
                    decoded.entropy.zero()
                }
            is DecodeResult.UnknownWord, DecodeResult.WrongWordCount, DecodeResult.ChecksumMismatch ->
                UnlockResult.Failed(KeyError.WRONG_RECOVERY_WORDS)
        }

    private suspend fun openRecoveryBlob(
        vault: VaultFile,
        recoverySecret: SecretBytes,
        newPassword: SecretChars,
    ): UnlockResult {
        val rwk = tools.keyDerivation.deriveRwk(recoverySecret)
        val opened = try {
            tools.aead.open(rwk, vault.wrappedMkRecovery, CryptoConstants.AAD_MK_RECOVERY)
        } finally {
            rwk.zero()
        }
        return when (opened) {
            OpenResult.AuthFailed -> {
                recordFailure()
                UnlockResult.Failed(KeyError.WRONG_RECOVERY_WORDS)
            }
            is OpenResult.Ok -> withNewPassword(vault, opened.plaintext, newPassword)
        }
    }

    private fun recordFailure() {
        failures++
        blockedUntilElapsed = context.bootInfo.elapsedRealtimeMillis() +
            LockoutPolicy.delayFor(failures).inWholeMilliseconds
    }

    /** Takes [mk]: it goes to the session on success and is zeroed otherwise. */
    private suspend fun withNewPassword(vault: VaultFile, mk: SecretBytes, newPassword: SecretChars): UnlockResult {
        var isKept = false
        try {
            val result = when (val derived = newPasswordDeriver.derive(newPassword)) {
                is NewPasswordResult.Rejected -> UnlockResult.PasswordRejected(derived.reason)
                is NewPasswordResult.Failed -> UnlockResult.Failed(derived.error)
                is NewPasswordResult.Ready -> wrapAndStore(vault, mk, derived)
            }
            isKept = result is UnlockResult.Unlocked
            return result
        } finally {
            if (!isKept) mk.zero()
        }
    }

    private suspend fun wrapAndStore(
        vault: VaultFile,
        mk: SecretBytes,
        derived: NewPasswordResult.Ready,
    ): UnlockResult =
        try {
            val inner = tools.aead.seal(derived.key, mk, CryptoConstants.AAD_MK_PASSWORD)
            // A missing or invalidated device key is exactly why the patient is recovering, so it is rebuilt.
            when (val wrapped = hardware.wrapRebuildingKey(vault.hwKeyAlias, inner)) {
                is HardwareWrapResult.Failed -> UnlockResult.Failed(wrapped.error)
                is HardwareWrapResult.Ok -> store(vault, mk, derived.params, wrapped.blob)
            }
        } finally {
            derived.key.zero()
        }

    private suspend fun store(vault: VaultFile, mk: SecretBytes, params: KdfParams, wrapped: ByteArray): UnlockResult {
        val updated = vault.copy(
            kdf = VaultFile.Kdf(vault.kdf.alg, params.salt, params.memoryKib, params.iterations, params.parallelism),
            wrappedMkPassword = wrapped,
            // Biometrics are cleared; the patient enables them again.
            wrappedMkBiometric = null,
            bioKeyAlias = null,
            failedAttempts = 0,
            lockoutUntilElapsed = 0,
            lockoutBootCount = context.bootInfo.bootCount(),
        )
        return if (context.store.write(updated) == VaultStoreResult.DONE) {
            biometricKeystore.deleteAlias(vault.bioKeyAlias ?: CryptoConstants.BIO_KEY_ALIAS)
            failures = 0
            blockedUntilElapsed = 0
            context.session.start(mk, context.clock.now(), context.bootInfo.elapsedRealtimeMillis())
            UnlockResult.Unlocked(needsBiometricRefresh = false)
        } else {
            UnlockResult.Failed(KeyError.STORAGE_FAILED)
        }
    }
}
