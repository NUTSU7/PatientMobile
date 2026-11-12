package com.estcomputer.patient.crypto

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import java.security.GeneralSecurityException
import java.security.UnrecoverableKeyException
import javax.crypto.AEADBadTagException
import javax.crypto.IllegalBlockSizeException

/** Null means the failure is a bug and the caller rethrows it. */
internal fun keystoreErrorOf(failure: GeneralSecurityException): KeystoreError? =
    when (failure) {
        is KeyPermanentlyInvalidatedException -> KeystoreError.HW_KEY_INVALIDATED
        // A key the provider cannot load is as unusable as an invalidated one.
        is UnrecoverableKeyException -> KeystoreError.HW_KEY_INVALIDATED
        is UserNotAuthenticatedException -> KeystoreError.DEVICE_LOCKED
        // ARH-11: a bad tag on the HW layer is not a wrong password and must not count as an attempt.
        is AEADBadTagException -> KeystoreError.RECOVERY_REQUIRED
        // The HW key needs an unlocked device; keystore2 reports a locked one this way at doFinal.
        is IllegalBlockSizeException ->
            if (isAuthenticationRequired(failure.cause)) KeystoreError.DEVICE_LOCKED else null
        else -> null
    }
