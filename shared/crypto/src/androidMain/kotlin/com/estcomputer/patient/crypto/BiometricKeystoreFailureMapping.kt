package com.estcomputer.patient.crypto

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import java.security.GeneralSecurityException
import java.security.UnrecoverableKeyException
import javax.crypto.AEADBadTagException

/** Null means the failure is a bug and the caller rethrows it. */
internal fun biometricKeystoreErrorOf(failure: GeneralSecurityException): BiometricKeystoreError? =
    when (failure) {
        is KeyPermanentlyInvalidatedException -> BiometricKeystoreError.KEY_INVALIDATED
        is UnrecoverableKeyException -> BiometricKeystoreError.KEY_INVALIDATED
        is UserNotAuthenticatedException -> BiometricKeystoreError.NOT_AUTHENTICATED
        is AEADBadTagException -> BiometricKeystoreError.BLOB_REJECTED
        else -> null
    }
