package com.estcomputer.patient.crypto

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.coroutines.withContext
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

internal class AndroidKeystoreCipher(
    context: Context,
    private val dispatchers: DispatcherProvider,
) : KeystoreCipher {
    private val hasStrongBox =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    private val keyStore: KeyStore by lazy { KeyStore.getInstance(PROVIDER).apply { load(null) } }
    private val keyguard = context.getSystemService(KeyguardManager::class.java)

    override suspend fun generateHwKey(alias: String): KeystoreGenerateResult =
        withContext(dispatchers.io) {
            if (!generateWithFallback(alias)) return@withContext KeystoreGenerateResult.DeviceNotSecure
            KeystoreGenerateResult.Ok(levelOf(requireNotNull(loadKey(alias)) { "generated key missing" }))
        }

    override suspend fun seal(alias: String, plaintext: SecretBytes, aad: String): KeystoreSealResult =
        withContext(dispatchers.io) {
            try {
                val key = loadKey(alias) ?: return@withContext KeystoreSealResult.Failed(KeystoreError.HW_KEY_MISSING)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                // No IV: the Keystore must pick it, and randomised encryption is never disabled.
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val iv = cipher.iv
                check(iv.size == CryptoConstants.GCM_NONCE_BYTES) { "unexpected keystore nonce size" }
                cipher.updateAAD(aad.encodeToByteArray())
                KeystoreSealResult.Ok(iv + plaintext.read { cipher.doFinal(it) })
            } catch (failure: GeneralSecurityException) {
                KeystoreSealResult.Failed(keystoreErrorOf(failure) ?: throw failure)
            }
        }

    override suspend fun open(alias: String, ciphertext: ByteArray, aad: String): KeystoreOpenResult =
        withContext(dispatchers.io) {
            try {
                val key = loadKey(alias) ?: return@withContext KeystoreOpenResult.Failed(KeystoreError.HW_KEY_MISSING)
                if (ciphertext.size < MIN_BLOB_BYTES) {
                    return@withContext KeystoreOpenResult.Failed(KeystoreError.RECOVERY_REQUIRED)
                }
                val iv = ciphertext.copyOfRange(0, CryptoConstants.GCM_NONCE_BYTES)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(CryptoConstants.GCM_TAG_BITS, iv))
                cipher.updateAAD(aad.encodeToByteArray())
                val body = ciphertext.size - CryptoConstants.GCM_NONCE_BYTES
                val plaintext = cipher.doFinal(ciphertext, CryptoConstants.GCM_NONCE_BYTES, body)
                KeystoreOpenResult.Ok(SecretBytes.wrap(plaintext))
            } catch (failure: GeneralSecurityException) {
                KeystoreOpenResult.Failed(keystoreErrorOf(failure) ?: throw failure)
            }
        }

    override suspend fun securityLevel(alias: String): SecurityLevel? =
        withContext(dispatchers.io) {
            try {
                loadKey(alias)?.let(::levelOf)
            } catch (_: GeneralSecurityException) {
                SecurityLevel.UNKNOWN
            }
        }

    override suspend fun deleteAlias(alias: String) {
        withContext(dispatchers.io) { keyStore.deleteEntry(alias) }
    }

    /** False when generation failed because the device has no secure lock screen. */
    private fun generateWithFallback(alias: String): Boolean {
        if (hasStrongBox && generateInStrongBox(alias)) return true
        return try {
            generate(alias, isStrongBox = false)
            true
        } catch (failure: ProviderException) {
            if (keyguard.isDeviceSecure) throw failure
            false
        }
    }

    // StrongBox can be advertised yet fail at generation, as a vendor ProviderException.
    private fun generateInStrongBox(alias: String): Boolean =
        try {
            generate(alias, isStrongBox = true)
            true
        } catch (_: StrongBoxUnavailableException) {
            false
        } catch (_: ProviderException) {
            false
        }

    private fun generate(alias: String, isStrongBox: Boolean) {
        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(CryptoConstants.KEY_SIZE_BYTES * Byte.SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUnlockedDeviceRequired(true)
            .setIsStrongBoxBacked(isStrongBox)
            .build()
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(spec)
        generator.generateKey()
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
