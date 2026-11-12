package com.estcomputer.patient.crypto

import android.os.Build
import android.security.KeyStoreException
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import java.security.GeneralSecurityException
import java.security.UnrecoverableKeyException
import javax.crypto.AEADBadTagException
import javax.crypto.IllegalBlockSizeException

/** Null means the failure is a bug and the caller rethrows it. */
internal fun biometricKeystoreErrorOf(failure: GeneralSecurityException): BiometricKeystoreError? =
    when (failure) {
        is KeyPermanentlyInvalidatedException -> BiometricKeystoreError.KEY_INVALIDATED
        is UnrecoverableKeyException -> BiometricKeystoreError.KEY_INVALIDATED
        is UserNotAuthenticatedException -> BiometricKeystoreError.NOT_AUTHENTICATED
        is AEADBadTagException -> BiometricKeystoreError.BLOB_REJECTED
        // keystore2 (API 31+) often reports an unauthenticated use this way instead.
        is IllegalBlockSizeException ->
            if (isAuthenticationRequired(failure.cause)) BiometricKeystoreError.NOT_AUTHENTICATED else null
        else -> null
    }

private const val KEYSTORE_EXCEPTION_CLASS = "android.security.KeyStoreException"

// The class is public only from API 33; before that every keystore failure at doFinal counts, which fails closed.
private fun isAuthenticationRequired(cause: Throwable?): Boolean =
    cause?.javaClass?.name == KEYSTORE_EXCEPTION_CLASS &&
        (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                (cause as KeyStoreException).numericErrorCode == KeyStoreException.ERROR_USER_AUTHENTICATION_REQUIRED
            )
