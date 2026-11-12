package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.port.BiometricChallenge

internal enum class BiometricKeystoreError {
    KEY_MISSING,

    /** Biometrics changed, or the key can no longer be loaded. The key is already deleted. */
    KEY_INVALIDATED,

    /** The use was not authenticated. A bug in the caller, or the authentication window had closed. */
    NOT_AUTHENTICATED,

    /** Wrong aad, tampered or truncated blob. */
    BLOB_REJECTED,
}

internal sealed interface BiometricKeystoreGenerateResult {
    data class Ok(val level: SecurityLevel) : BiometricKeystoreGenerateResult

    /** No secure lock screen or no strong biometric enrolled, so a key that needs one cannot be created. */
    data object NoBiometricEnrolled : BiometricKeystoreGenerateResult
}

internal sealed interface BiometricKeystoreBeginResult {
    class Ready(val challenge: BiometricChallenge) : BiometricKeystoreBeginResult {
        override fun toString(): String = "Ready"
    }

    data class Failed(val error: BiometricKeystoreError) : BiometricKeystoreBeginResult
}

internal sealed interface BiometricKeystoreSealResult {
    class Ok(val blob: ByteArray) : BiometricKeystoreSealResult {
        override fun toString(): String = "Ok(***)"
    }

    data class Failed(val error: BiometricKeystoreError) : BiometricKeystoreSealResult
}

internal sealed interface BiometricKeystoreOpenResult {
    /** The caller zeroes [plaintext]. */
    class Ok(val plaintext: SecretBytes) : BiometricKeystoreOpenResult {
        override fun toString(): String = "Ok(***)"
    }

    data class Failed(val error: BiometricKeystoreError) : BiometricKeystoreOpenResult
}
