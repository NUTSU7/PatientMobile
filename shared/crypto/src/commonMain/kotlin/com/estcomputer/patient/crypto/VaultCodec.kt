package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.text.CharacterCodingException
import kotlin.time.Instant

internal class VaultCodec {
    fun encode(vault: VaultFile): ByteArray = JSON.encodeToString(VaultFile.serializer(), vault).encodeToByteArray()

    /** Any parse or validation failure is `VAULT_CORRUPT`; the vault is never repaired. */
    fun decode(bytes: ByteArray): VaultDecodeResult {
        val vault = if (bytes.size > MAX_VAULT_BYTES) null else parse(bytes)
        return if (vault != null && isValid(vault)) {
            VaultDecodeResult.Ok(vault)
        } else {
            VaultDecodeResult.Failed(KeyError.VAULT_CORRUPT)
        }
    }

    private fun parse(bytes: ByteArray): VaultFile? =
        try {
            JSON.decodeFromString(VaultFile.serializer(), bytes.decodeToString(throwOnInvalidSequence = true))
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: CharacterCodingException) {
            null
        }

    private fun isValid(vault: VaultFile): Boolean =
        vault.format == CryptoConstants.VAULT_FORMAT &&
            isInstant(vault.createdAt) &&
            vault.kdf.alg == KDF_ALGORITHM &&
            vault.kdfParams.isWithinBounds &&
            vault.wrappedMkPassword.size == WRAPPED_MK_PASSWORD_BYTES &&
            vault.wrappedMkRecovery.size == WRAPPED_MK_RECOVERY_BYTES &&
            vault.exportPublicKey.size == CryptoConstants.KEY_SIZE_BYTES &&
            isValidBiometricPair(vault.wrappedMkBiometric, vault.bioKeyAlias) &&
            vault.failedAttempts >= 0 &&
            isValidWipeThreshold(vault.wipeAfterFailedAttempts) &&
            vault.lockoutUntilElapsed >= 0 &&
            vault.lockoutBootCount >= 0

    private fun isValidBiometricPair(wrapped: ByteArray?, alias: String?): Boolean =
        if (wrapped == null || alias == null) {
            wrapped == null && alias == null
        } else {
            wrapped.size > MIN_WRAPPED_MK_BIOMETRIC_BYTES && alias.isNotBlank()
        }

    private fun isInstant(text: String): Boolean =
        try {
            Instant.parse(text)
            true
        } catch (_: IllegalArgumentException) {
            false
        }

    private fun isValidWipeThreshold(value: Int): Boolean =
        value == WIPE_DISABLED || value in CryptoConstants.WIPE_AFTER_MIN..CryptoConstants.WIPE_AFTER_MAX

    private companion object {
        const val KDF_ALGORITHM = "argon2id"
        const val WIPE_DISABLED = 0
        const val MAX_VAULT_BYTES = 65_536

        // nonce + (nonce + MK + tag) + tag: password layer wrapped again by the HW key.
        const val WRAPPED_MK_PASSWORD_BYTES = 88

        // nonce + MK + tag.
        const val WRAPPED_MK_RECOVERY_BYTES = 60

        // nonce + MK + tag, plus at least one byte of the policy JSON.
        const val MIN_WRAPPED_MK_BIOMETRIC_BYTES =
            CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.KEY_SIZE_BYTES + CryptoConstants.GCM_TAG_BYTES

        val JSON = Json {
            ignoreUnknownKeys = false
            explicitNulls = true
            encodeDefaults = true
        }
    }
}
