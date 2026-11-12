package com.estcomputer.patient.crypto

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import java.security.GeneralSecurityException
import java.security.UnrecoverableKeyException
import javax.crypto.AEADBadTagException

/** Null means the failure is a bug and the caller rethrows it. */
internal fun keystoreErrorOf(failure: GeneralSecurityException): KeystoreError? =
    when (failure) {
        is KeyPermanentlyInvalidatedException -> KeystoreError.HW_KEY_INVALIDATED
        // A key the provider cannot load is as unusable as an invalidated one.
        is UnrecoverableKeyException -> KeystoreError.HW_KEY_INVALIDATED
        is UserNotAuthenticatedException -> KeystoreError.DEVICE_LOCKED
        // ARH-11: a bad tag on the HW layer is not a wrong password and must not count as an attempt.
        is AEADBadTagException -> KeystoreError.RECOVERY_REQUIRED
        else -> null
    }
