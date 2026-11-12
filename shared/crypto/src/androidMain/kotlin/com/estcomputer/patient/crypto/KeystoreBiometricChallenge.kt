package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.BiometricChallenge
import javax.crypto.Cipher

/**
 * A [cipher] already initialised on the biometric key, waiting for the patient to authenticate. Public so
 * the prompt in `composeApp` can wrap it in a `CryptoObject`.
 */
public class KeystoreBiometricChallenge internal constructor(
    public val cipher: Cipher,
    internal val mode: Mode,
) : BiometricChallenge {
    internal enum class Mode { ENCRYPT, DECRYPT }

    override fun toString(): String = "KeystoreBiometricChallenge"
}
