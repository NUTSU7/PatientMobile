package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

/** Where the key lives. Display only; the app never branches on it except to warn about [SOFTWARE]. */
internal enum class SecurityLevel {
    STRONGBOX,
    TRUSTED_ENVIRONMENT,
    SOFTWARE,
    UNKNOWN,
}

internal enum class KeystoreError {
    HW_KEY_MISSING,
    HW_KEY_INVALIDATED,
    DEVICE_LOCKED,
    RECOVERY_REQUIRED,
}

internal sealed interface KeystoreGenerateResult {
    data class Ok(val level: SecurityLevel) : KeystoreGenerateResult

    /** The device has no secure lock screen, now or since the caller last checked. */
    data object DeviceNotSecure : KeystoreGenerateResult
}

internal sealed interface KeystoreSealResult {
    class Ok(val blob: ByteArray) : KeystoreSealResult {
        override fun toString(): String = "Ok(***)"
    }

    data class Failed(val error: KeystoreError) : KeystoreSealResult
}

internal sealed interface KeystoreOpenResult {
    /** The caller zeroes [plaintext]. */
    class Ok(val plaintext: SecretBytes) : KeystoreOpenResult {
        override fun toString(): String = "Ok(***)"
    }

    data class Failed(val error: KeystoreError) : KeystoreOpenResult
}
