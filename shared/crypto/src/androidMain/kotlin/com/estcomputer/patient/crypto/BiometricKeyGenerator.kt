package com.estcomputer.patient.crypto

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.InvalidAlgorithmParameterException
import java.security.ProviderException
import javax.crypto.KeyGenerator

internal class BiometricKeyGenerator(context: Context) {
    private val hasStrongBox =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

    /** False when no secure lock screen or strong biometric is enrolled. */
    fun generate(alias: String): Boolean {
        if (hasStrongBox && tryStrongBox(alias)) return true
        return try {
            build(alias, isStrongBox = false)
            true
        } catch (_: InvalidAlgorithmParameterException) {
            false
        }
    }

    // StrongBox can be advertised yet fail at generation, as a vendor ProviderException.
    private fun tryStrongBox(alias: String): Boolean =
        try {
            build(alias, isStrongBox = true)
            true
        } catch (_: StrongBoxUnavailableException) {
            false
        } catch (_: ProviderException) {
            false
        } catch (_: InvalidAlgorithmParameterException) {
            false
        }

    private fun build(alias: String, isStrongBox: Boolean) {
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(CryptoConstants.KEY_SIZE_BYTES * Byte.SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .setIsStrongBoxBacked(isStrongBox)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(AUTH_EVERY_USE, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            // API 29: -1 binds every use to a fingerprint through the CryptoObject.
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val AUTH_EVERY_USE = 0
    }
}
