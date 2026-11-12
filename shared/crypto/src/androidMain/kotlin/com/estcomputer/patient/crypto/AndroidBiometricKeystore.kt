package com.estcomputer.patient.crypto

import android.content.Context
import android.os.Build
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.port.BiometricChallenge
import kotlinx.coroutines.withContext
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

internal class AndroidBiometricKeystore(
    context: Context,
    private val dispatchers: DispatcherProvider,
) : BiometricKeystore {
    private val generator = BiometricKeyGenerator(context)
    private val keyStore: KeyStore by lazy { KeyStore.getInstance(PROVIDER).apply { load(null) } }

    override suspend fun generateBioKey(alias: String): BiometricKeystoreGenerateResult =
        withContext(dispatchers.io) {
            if (!generator.generate(alias)) return@withContext BiometricKeystoreGenerateResult.NoBiometricEnrolled
            val key = requireNotNull(loadKey(alias)) { "generated key missing" }
            BiometricKeystoreGenerateResult.Ok(levelOf(key))
        }

    override suspend fun beginSeal(alias: String): BiometricKeystoreBeginResult =
        withContext(dispatchers.io) {
            try {
                val key = loadKey(alias)
                    ?: return@withContext BiometricKeystoreBeginResult.Failed(BiometricKeystoreError.KEY_MISSING)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                // No IV: the Keystore must pick it, and randomised encryption is never disabled.
                cipher.init(Cipher.ENCRYPT_MODE, key)
                BiometricKeystoreBeginResult.Ready(
                    KeystoreBiometricChallenge(cipher, KeystoreBiometricChallenge.Mode.ENCRYPT),
                )
            } catch (failure: GeneralSecurityException) {
                BiometricKeystoreBeginResult.Failed(errorOf(alias, failure))
            }
        }

    override suspend fun beginOpen(alias: String, blob: ByteArray): BiometricKeystoreBeginResult =
        withContext(dispatchers.io) {
            try {
                val key = loadKey(alias)
                    ?: return@withContext BiometricKeystoreBeginResult.Failed(BiometricKeystoreError.KEY_MISSING)
                if (blob.size < MIN_BLOB_BYTES) {
                    return@withContext BiometricKeystoreBeginResult.Failed(BiometricKeystoreError.BLOB_REJECTED)
                }
                val iv = blob.copyOfRange(0, CryptoConstants.GCM_NONCE_BYTES)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(CryptoConstants.GCM_TAG_BITS, iv))
                BiometricKeystoreBeginResult.Ready(
                    KeystoreBiometricChallenge(cipher, KeystoreBiometricChallenge.Mode.DECRYPT),
                )
            } catch (failure: GeneralSecurityException) {
                BiometricKeystoreBeginResult.Failed(errorOf(alias, failure))
            }
        }

    override suspend fun seal(
        challenge: BiometricChallenge,
        plaintext: SecretBytes,
        aad: String,
    ): BiometricKeystoreSealResult {
        val cipher = cipherOf(challenge, KeystoreBiometricChallenge.Mode.ENCRYPT)
        return withContext(dispatchers.io) {
            try {
                // AAD goes in after authentication: on an authenticated key every cipher call needs it.
                cipher.updateAAD(aad.encodeToByteArray())
                val ciphertext = plaintext.read { cipher.doFinal(it) }
                val iv = cipher.iv
                check(iv.size == CryptoConstants.GCM_NONCE_BYTES) { "unexpected keystore nonce size" }
                BiometricKeystoreSealResult.Ok(iv + ciphertext)
            } catch (failure: GeneralSecurityException) {
                BiometricKeystoreSealResult.Failed(biometricKeystoreErrorOf(failure) ?: throw failure)
            }
        }
    }

    override suspend fun open(
        challenge: BiometricChallenge,
        blob: ByteArray,
        aad: String,
    ): BiometricKeystoreOpenResult {
        val cipher = cipherOf(challenge, KeystoreBiometricChallenge.Mode.DECRYPT)
        return withContext(dispatchers.io) {
            try {
                if (blob.size < MIN_BLOB_BYTES) {
                    return@withContext BiometricKeystoreOpenResult.Failed(BiometricKeystoreError.BLOB_REJECTED)
                }
                cipher.updateAAD(aad.encodeToByteArray())
                val body = blob.size - CryptoConstants.GCM_NONCE_BYTES
                val plaintext = cipher.doFinal(blob, CryptoConstants.GCM_NONCE_BYTES, body)
                BiometricKeystoreOpenResult.Ok(SecretBytes.wrap(plaintext))
            } catch (failure: GeneralSecurityException) {
                BiometricKeystoreOpenResult.Failed(biometricKeystoreErrorOf(failure) ?: throw failure)
            }
        }
    }

    override suspend fun deleteAlias(alias: String) {
        withContext(dispatchers.io) { keyStore.deleteEntry(alias) }
    }

    private fun cipherOf(challenge: BiometricChallenge, mode: KeystoreBiometricChallenge.Mode): Cipher {
        val own = challenge as? KeystoreBiometricChallenge
        require(own != null && own.mode == mode) { "challenge does not belong to this operation" }
        return own.cipher
    }

    // An invalidated key is useless for good, so it is removed here and the caller drops the vault fields.
    private fun errorOf(alias: String, failure: GeneralSecurityException): BiometricKeystoreError {
        val error = biometricKeystoreErrorOf(failure) ?: throw failure
        if (error == BiometricKeystoreError.KEY_INVALIDATED) keyStore.deleteEntry(alias)
        return error
    }

    private fun loadKey(alias: String): SecretKey? = keyStore.getKey(alias, null) as SecretKey?

    private fun levelOf(key: SecretKey): SecurityLevel {
        val factory = SecretKeyFactory.getInstance(key.algorithm, PROVIDER)
        val info = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (info.securityLevel) {
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> SecurityLevel.STRONGBOX
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> SecurityLevel.TRUSTED_ENVIRONMENT
                KeyProperties.SECURITY_LEVEL_SOFTWARE -> SecurityLevel.SOFTWARE
                else -> SecurityLevel.UNKNOWN
            }
        } else {
            @Suppress("DEPRECATION")
            if (info.isInsideSecureHardware) SecurityLevel.TRUSTED_ENVIRONMENT else SecurityLevel.SOFTWARE
        }
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val MIN_BLOB_BYTES = CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.GCM_TAG_BYTES
    }
}
