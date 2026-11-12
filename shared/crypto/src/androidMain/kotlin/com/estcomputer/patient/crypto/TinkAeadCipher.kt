package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.google.crypto.tink.Aead
import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKey
import com.google.crypto.tink.aead.AesGcmParameters
import java.security.GeneralSecurityException
import com.google.crypto.tink.util.SecretBytes as TinkSecretBytes

// Tink copies the key into its own objects, which can't be zeroed; accepted risk for in-memory keys.
internal class TinkAeadCipher : AeadCipher {
    override fun seal(key: SecretBytes, plaintext: SecretBytes, aad: String): ByteArray {
        val aead = aead(key)
        return plaintext.read { aead.encrypt(it, aad.encodeToByteArray()) }
    }

    override fun open(key: SecretBytes, ciphertext: ByteArray, aad: String): OpenResult =
        open(key, ciphertext, aad.encodeToByteArray())

    /** Byte form kept for the Wycheproof vectors, whose associated data isn't always UTF-8. */
    fun open(key: SecretBytes, ciphertext: ByteArray, aad: ByteArray): OpenResult {
        if (ciphertext.size < MIN_CIPHERTEXT_BYTES) return OpenResult.AuthFailed
        val aead = aead(key)
        return try {
            OpenResult.Ok(SecretBytes.wrap(aead.decrypt(ciphertext, aad)))
        } catch (_: GeneralSecurityException) {
            OpenResult.AuthFailed
        }
    }

    private fun aead(key: SecretBytes): Aead {
        val keyBytes = key.read { TinkSecretBytes.copyFrom(it, InsecureSecretKeyAccess.get()) }
        val aesGcmKey = AesGcmKey.builder().setParameters(PARAMETERS).setKeyBytes(keyBytes).build()
        val entry = KeysetHandle.importKey(aesGcmKey).withRandomId().makePrimary()
        val keyset = KeysetHandle.newBuilder().addEntry(entry).build()
        return keyset.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private companion object {
        init {
            AeadConfig.register()
        }

        const val MIN_CIPHERTEXT_BYTES = CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.GCM_TAG_BYTES

        // NO_PREFIX keeps Tink's 5-byte key-id prefix out of the persisted format.
        val PARAMETERS: AesGcmParameters = AesGcmParameters.builder()
            .setKeySizeBytes(CryptoConstants.KEY_SIZE_BYTES)
            .setIvSizeBytes(CryptoConstants.GCM_NONCE_BYTES)
            .setTagSizeBytes(CryptoConstants.GCM_TAG_BYTES)
            .setVariant(AesGcmParameters.Variant.NO_PREFIX)
            .build()
    }
}
