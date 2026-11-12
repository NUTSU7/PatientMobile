package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.BiometricEnrolmentResult
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.port.BiometricChallenge
import kotlinx.coroutines.sync.withLock

/** Enable, refresh and disable. Refresh is enable again: it reuses the key and replaces the blob. */
internal class BiometricEnrolmentFlow(
    private val keystore: BiometricKeystore,
    private val cleaner: BiometricCleaner,
    private val context: VaultContext,
) {
    suspend fun begin(): BiometricBeginResult =
        context.mutex.withLock {
            check(context.session.isUnlocked) { "vault is locked" }
            when (val read = context.store.read()) {
                is VaultReadResult.Ok -> beginSeal(read.vault)
                VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                    BiometricBeginResult.Failed(KeyError.VAULT_CORRUPT)
            }
        }

    suspend fun complete(challenge: BiometricChallenge): BiometricEnrolmentResult =
        context.mutex.withLock {
            check(context.session.isUnlocked) { "vault is locked" }
            when (val read = context.store.read()) {
                is VaultReadResult.Ok -> seal(read.vault, challenge)
                VaultReadResult.Absent, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                    BiometricEnrolmentResult.Failed(KeyError.VAULT_CORRUPT)
            }
        }

    suspend fun disable() {
        context.mutex.withLock {
            val vault = (context.store.read() as? VaultReadResult.Ok)?.vault
            if (vault == null) keystore.deleteAlias(CryptoConstants.BIO_KEY_ALIAS) else cleaner.clear(vault)
        }
    }

    private suspend fun beginSeal(vault: VaultFile): BiometricBeginResult {
        val alias = vault.bioKeyAlias
        return if (alias == null) {
            createKeyAndBegin(CryptoConstants.BIO_KEY_ALIAS)
        } else {
            when (val begun = keystore.beginSeal(alias)) {
                is BiometricKeystoreBeginResult.Ready -> BiometricBeginResult.Ready(begun.challenge)
                is BiometricKeystoreBeginResult.Failed ->
                    if (begun.error == BiometricKeystoreError.KEY_MISSING) {
                        createKeyAndBegin(alias)
                    } else {
                        cleaner.clear(vault)
                        BiometricBeginResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
                    }
            }
        }
    }

    private suspend fun createKeyAndBegin(alias: String): BiometricBeginResult =
        when (keystore.generateBioKey(alias)) {
            BiometricKeystoreGenerateResult.NoBiometricEnrolled ->
                BiometricBeginResult.Failed(KeyError.BIOMETRIC_NOT_ENROLLED)
            is BiometricKeystoreGenerateResult.Ok ->
                when (val begun = keystore.beginSeal(alias)) {
                    is BiometricKeystoreBeginResult.Ready -> BiometricBeginResult.Ready(begun.challenge)
                    is BiometricKeystoreBeginResult.Failed ->
                        BiometricBeginResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
                }
        }

    private suspend fun seal(vault: VaultFile, challenge: BiometricChallenge): BiometricEnrolmentResult {
        val payload = BioPayload.encode(
            context.session.requireMk(),
            BioPolicyData(context.bootInfo.bootCount(), context.session.passwordUnlockAt),
        )
        try {
            return when (val sealed = keystore.seal(challenge, payload, CryptoConstants.AAD_MK_BIO)) {
                is BiometricKeystoreSealResult.Ok -> store(vault, sealed.blob)
                is BiometricKeystoreSealResult.Failed -> failedSeal(vault, sealed.error)
            }
        } finally {
            payload.zero()
        }
    }

    private suspend fun failedSeal(vault: VaultFile, error: BiometricKeystoreError): BiometricEnrolmentResult {
        if (error.isUnusable()) cleaner.clear(vault)
        return BiometricEnrolmentResult.Failed(KeyError.BIOMETRIC_INVALIDATED)
    }

    private suspend fun store(vault: VaultFile, blob: ByteArray): BiometricEnrolmentResult {
        val updated = vault.copy(
            wrappedMkBiometric = blob,
            bioKeyAlias = vault.bioKeyAlias ?: CryptoConstants.BIO_KEY_ALIAS,
        )
        return if (context.store.write(updated) == VaultStoreResult.DONE) {
            BiometricEnrolmentResult.Enabled
        } else {
            BiometricEnrolmentResult.Failed(KeyError.STORAGE_FAILED)
        }
    }
}
