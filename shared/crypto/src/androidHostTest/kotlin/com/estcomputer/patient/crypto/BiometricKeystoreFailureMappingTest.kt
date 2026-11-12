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

class BiometricKeystoreFailureMappingTest {
    @Test
    fun biometricKeystoreErrorOf_invalidatedKey_isKeyInvalidated() {
        assertEquals(
            BiometricKeystoreError.KEY_INVALIDATED,
            biometricKeystoreErrorOf(KeyPermanentlyInvalidatedException()),
        )
    }

    @Test
    fun biometricKeystoreErrorOf_unrecoverableKey_isKeyInvalidated() {
        assertEquals(
            BiometricKeystoreError.KEY_INVALIDATED,
            biometricKeystoreErrorOf(UnrecoverableKeyException()),
        )
    }

    @Test
    fun biometricKeystoreErrorOf_notAuthenticated_isNotAuthenticated() {
        assertEquals(
            BiometricKeystoreError.NOT_AUTHENTICATED,
            biometricKeystoreErrorOf(UserNotAuthenticatedException()),
        )
    }

    @Test
    fun biometricKeystoreErrorOf_badTag_isBlobRejected() {
        assertEquals(BiometricKeystoreError.BLOB_REJECTED, biometricKeystoreErrorOf(AEADBadTagException()))
    }

    @Test
    fun biometricKeystoreErrorOf_unknownFailure_isNull() {
        assertNull(biometricKeystoreErrorOf(IllegalBlockSizeException()))
        assertNull(biometricKeystoreErrorOf(GeneralSecurityException()))
    }
}
