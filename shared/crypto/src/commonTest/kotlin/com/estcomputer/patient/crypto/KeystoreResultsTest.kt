package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class KeystoreResultsTest {
    @Test
    fun sealOk_toString_isRedacted() {
        assertEquals("Ok(***)", KeystoreSealResult.Ok(byteArrayOf(1, 2, 3)).toString())
    }

    @Test
    fun openOk_toString_isRedacted() {
        assertEquals("Ok(***)", KeystoreOpenResult.Ok(SecretBytes.copyOf(byteArrayOf(1, 2, 3))).toString())
    }

    @Test
    fun failedResults_exhaustiveWhen_compilesWithoutElse() {
        val seal: KeystoreSealResult = KeystoreSealResult.Failed(KeystoreError.HW_KEY_MISSING)
        val open: KeystoreOpenResult = KeystoreOpenResult.Failed(KeystoreError.RECOVERY_REQUIRED)
        val sealCode = when (seal) {
            is KeystoreSealResult.Ok -> null
            is KeystoreSealResult.Failed -> seal.error
        }
        val openCode = when (open) {
            is KeystoreOpenResult.Ok -> null
            is KeystoreOpenResult.Failed -> open.error
        }
        assertEquals(KeystoreError.HW_KEY_MISSING, sealCode)
        assertEquals(KeystoreError.RECOVERY_REQUIRED, openCode)
    }
}
