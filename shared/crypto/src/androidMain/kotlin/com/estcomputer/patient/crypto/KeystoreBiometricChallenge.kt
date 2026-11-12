package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.BiometricChallenge
import javax.crypto.Cipher

/**
 * A [cipher] already initialised on the biometric key, waiting for the patient to authenticate. Public so
 * the prompt in `composeApp` can wrap it in a `CryptoObject`. Only `BiometricKeystore.seal` and `open` may
 * use the cipher; nothing else may call `doFinal` on it.
 */
public class KeystoreBiometricChallenge internal constructor(
    public val cipher: Cipher,
    internal val mode: Mode,
    // Read at init: the nonce of a sealed blob must not depend on the cipher state after doFinal.
    internal val iv: ByteArray,
) : BiometricChallenge {
    internal enum class Mode { ENCRYPT, DECRYPT }

    override fun toString(): String = "KeystoreBiometricChallenge"
}

internal fun BiometricChallenge.requireOwn(mode: KeystoreBiometricChallenge.Mode): KeystoreBiometricChallenge {
    val own = this as? KeystoreBiometricChallenge
    require(own != null && own.mode == mode) { "challenge does not belong to this operation" }
    return own
}
