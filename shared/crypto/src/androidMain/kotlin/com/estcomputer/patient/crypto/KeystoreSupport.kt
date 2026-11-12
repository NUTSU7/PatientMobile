package com.estcomputer.patient.crypto

import android.os.Build
import android.security.KeyStoreException
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.ProviderException
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

internal const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"

/**
 * Runs [generate] with StrongBox when the device has it, and without when StrongBox cannot make the key.
 * Failures of the final attempt reach the caller.
 */
internal fun generateWithStrongBoxFallback(hasStrongBox: Boolean, generate: (isStrongBox: Boolean) -> Unit) {
    if (hasStrongBox && isGeneratedInStrongBox(generate)) return
    generate(false)
}

// StrongBox can be advertised yet fail at generation, as a vendor ProviderException.
private fun isGeneratedInStrongBox(generate: (isStrongBox: Boolean) -> Unit): Boolean =
    try {
        generate(true)
        true
    } catch (_: StrongBoxUnavailableException) {
        false
    } catch (_: ProviderException) {
        false
    }

internal fun securityLevelOf(key: SecretKey): SecurityLevel {
    val factory = SecretKeyFactory.getInstance(key.algorithm, ANDROID_KEYSTORE_PROVIDER)
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

private const val KEYSTORE_EXCEPTION_CLASS = "android.security.KeyStoreException"

/**
 * True when [cause] says the key needs an authenticated user or an unlocked device. keystore2 (API 31+) wraps
 * that in an `IllegalBlockSizeException` at `doFinal` instead of throwing `UserNotAuthenticatedException`.
 * The class is public only from API 33; before that every keystore failure at `doFinal` counts. That opens
 * nothing and counts no password attempt, so it fails closed.
 */
internal fun isAuthenticationRequired(cause: Throwable?): Boolean =
    cause?.javaClass?.name == KEYSTORE_EXCEPTION_CLASS &&
        (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                (cause as KeyStoreException).numericErrorCode == KeyStoreException.ERROR_USER_AUTHENTICATION_REQUIRED
            )
