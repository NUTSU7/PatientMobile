package com.estcomputer.patient.crypto

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.InvalidAlgorithmParameterException
import javax.crypto.KeyGenerator

internal class BiometricKeyGenerator(context: Context) {
    private val hasStrongBox =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    private val keyguard = context.getSystemService(KeyguardManager::class.java)
    private val biometrics = context.getSystemService(BiometricManager::class.java)

    /** False when no secure lock screen or strong biometric is enrolled. */
    fun generate(alias: String): Boolean =
        try {
            generateWithStrongBoxFallback(hasStrongBox) { build(alias, it) }
            true
        } catch (failure: InvalidAlgorithmParameterException) {
            // Only a device that cannot make the key may report "not enrolled"; otherwise the spec is wrong.
            if (isStrongBiometricEnrolled()) throw failure
            false
        }

    private fun isStrongBiometricEnrolled(): Boolean = keyguard.isDeviceSecure && canAuthenticateStrong()

    private fun canAuthenticateStrong(): Boolean {
        val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            biometrics.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            biometrics.canAuthenticate()
        }
        return status == BiometricManager.BIOMETRIC_SUCCESS
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
        const val PROVIDER = ANDROID_KEYSTORE_PROVIDER
        const val AUTH_EVERY_USE = 0
    }
}
