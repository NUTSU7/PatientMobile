package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BiometricChallenge
import kotlinx.coroutines.sync.withLock

internal class BiometricUnlockFlow(
    private val keystore: BiometricKeystore,
    private val policy: BioPolicy,
    private val cleaner: BiometricCleaner,
    private val context: VaultContext,
) {
    suspend fun begin(): BiometricBeginResult =
        context.mutex.withLock {
            when (val read = context.store.read()) {
                is VaultReadResult.Ok -> beginOpen(read.vault)
                VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                    BiometricBeginResult.Failed(KeyError.VAULT_CORRUPT)
            }
        }

    suspend fun complete(challenge: BiometricChallenge): UnlockResult =
        context.mutex.withLock {
            when (val read = context.store.read()) {
                is VaultReadResult.Ok -> open(read.vault, challenge)
                VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                    UnlockResult.Failed(KeyError.VAULT_CORRUPT)
            }
        }

    private suspend fun beginOpen(vault: VaultFile): BiometricBeginResult {
        val alias = vault.bioKeyAlias
        val blob = vault.wrappedMkBiometric
        return if (alias == null || blob == null) {
            BiometricBeginResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
        } else {
            when (val begun = keystore.beginOpen(alias, blob)) {
                is BiometricKeystoreBeginResult.Ready -> BiometricBeginResult.Ready(begun.challenge)
                is BiometricKeystoreBeginResult.Failed -> {
                    cleaner.clear(vault)
                    BiometricBeginResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
                }
            }
        }
    }

    private suspend fun open(vault: VaultFile, challenge: BiometricChallenge): UnlockResult {
        val blob = vault.wrappedMkBiometric
        return if (blob == null) {
            UnlockResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
        } else {
            when (val opened = keystore.open(challenge, blob, CryptoConstants.AAD_MK_BIO)) {
                is BiometricKeystoreOpenResult.Ok -> unlockWith(vault, opened.plaintext)
                is BiometricKeystoreOpenResult.Failed -> failedOpen(vault, opened.error)
            }
        }
    }

    private suspend fun failedOpen(vault: VaultFile, error: BiometricKeystoreError): UnlockResult {
        if (error.isUnusable()) cleaner.clear(vault)
        return UnlockResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
    }

    private suspend fun unlockWith(vault: VaultFile, payload: SecretBytes): UnlockResult =
        when (val decoded = BioPayload.decode(payload)) {
            BioPayloadDecodeResult.Malformed -> {
                cleaner.clear(vault)
                UnlockResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
            }
            is BioPayloadDecodeResult.Ok ->
                if (policy.requiresPassword(decoded.policy)) {
                    decoded.mk.zero()
                    UnlockResult.Failed(KeyError.BIOMETRIC_POLICY_EXPIRED)
                } else {
                    // Keeps the recorded password time, so a biometric unlock never extends the 7 days.
                    context.session.start(
                        decoded.mk,
                        decoded.policy.passwordUnlockAt,
                        context.bootInfo.elapsedRealtimeMillis(),
                    )
                    UnlockResult.Unlocked(needsBiometricRefresh = false)
                }
        }
}
