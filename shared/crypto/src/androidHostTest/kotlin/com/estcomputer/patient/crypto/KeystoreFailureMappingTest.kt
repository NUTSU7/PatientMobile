package com.estcomputer.patient.crypto

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import java.security.GeneralSecurityException
import java.security.UnrecoverableKeyException
import javax.crypto.AEADBadTagException
import javax.crypto.IllegalBlockSizeException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeystoreFailureMappingTest {
    @Test
    fun keystoreErrorOf_invalidatedKey_isHwKeyInvalidated() {
        assertEquals(KeystoreError.HW_KEY_INVALIDATED, keystoreErrorOf(KeyPermanentlyInvalidatedException()))
    }

    @Test
    fun keystoreErrorOf_unrecoverableKey_isHwKeyInvalidated() {
        assertEquals(KeystoreError.HW_KEY_INVALIDATED, keystoreErrorOf(UnrecoverableKeyException()))
    }

    @Test
    fun keystoreErrorOf_lockedDevice_isDeviceLocked() {
        assertEquals(KeystoreError.DEVICE_LOCKED, keystoreErrorOf(UserNotAuthenticatedException()))
    }

    @Test
    fun keystoreErrorOf_badTag_isRecoveryRequired() {
        assertEquals(KeystoreError.RECOVERY_REQUIRED, keystoreErrorOf(AEADBadTagException()))
    }

    @Test
    fun keystoreErrorOf_unknownFailure_isNull() {
        assertNull(keystoreErrorOf(IllegalBlockSizeException()))
        assertNull(keystoreErrorOf(GeneralSecurityException()))
    }
}
