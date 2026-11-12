package com.estcomputer.patient.crypto

internal fun BiometricKeystoreError.isUnusable(): Boolean =
    when (this) {
        BiometricKeystoreError.KEY_MISSING,
        BiometricKeystoreError.KEY_INVALIDATED,
        BiometricKeystoreError.BLOB_REJECTED,
        -> true
        BiometricKeystoreError.NOT_AUTHENTICATED -> false
    }

/**
 * Drops an unusable biometric key and blob, so the patient falls back to the password and can enable
 * biometrics again.
 */
internal class BiometricCleaner(private val keystore: BiometricKeystore, private val context: VaultContext) {
    // The key goes first (ARH-09 order); a failed vault write only leaves a blob that no key can open.
    suspend fun clear(vault: VaultFile) {
        keystore.deleteAlias(vault.bioKeyAlias ?: CryptoConstants.BIO_KEY_ALIAS)
        if (vault.wrappedMkBiometric != null || vault.bioKeyAlias != null) {
            context.store.write(vault.copy(wrappedMkBiometric = null, bioKeyAlias = null))
        }
    }
}
