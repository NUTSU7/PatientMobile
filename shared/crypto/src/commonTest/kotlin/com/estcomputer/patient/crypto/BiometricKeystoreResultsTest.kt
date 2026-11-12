package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.port.BiometricChallenge
import kotlin.test.Test
import kotlin.test.assertEquals

class BiometricKeystoreResultsTest {
    @Test
    fun results_toString_areRedacted() {
        assertEquals("Ok(***)", BiometricKeystoreSealResult.Ok(byteArrayOf(1, 2, 3)).toString())
        assertEquals(
            "Ok(***)",
            BiometricKeystoreOpenResult.Ok(SecretBytes.copyOf(byteArrayOf(1, 2, 3))).toString(),
        )
        assertEquals("Ready", BiometricKeystoreBeginResult.Ready(object : BiometricChallenge {}).toString())
    }

    @Test
    fun failedResults_exhaustiveWhen_compilesWithoutElse() {
        val begin: BiometricKeystoreBeginResult =
            BiometricKeystoreBeginResult.Failed(BiometricKeystoreError.KEY_MISSING)
        val generate: BiometricKeystoreGenerateResult = BiometricKeystoreGenerateResult.NoBiometricEnrolled
        val beginCode = when (begin) {
            is BiometricKeystoreBeginResult.Ready -> null
            is BiometricKeystoreBeginResult.Failed -> begin.error
        }
        val enrolled = when (generate) {
            is BiometricKeystoreGenerateResult.Ok -> true
            BiometricKeystoreGenerateResult.NoBiometricEnrolled -> false
        }
        assertEquals(BiometricKeystoreError.KEY_MISSING, beginCode)
        assertEquals(false, enrolled)
    }
}
