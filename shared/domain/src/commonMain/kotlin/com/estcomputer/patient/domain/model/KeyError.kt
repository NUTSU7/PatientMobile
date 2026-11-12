package com.estcomputer.patient.domain.model

/** Data-free failure codes shared by every key operation. */
public enum class KeyError {
    WRONG_PASSWORD,
    WRONG_RECOVERY_WORDS,
    RECOVERY_REQUIRED,
    HW_KEY_INVALIDATED,
    DEVICE_LOCKED,
    VAULT_CORRUPT,
    BIOMETRIC_INVALIDATED,
    BIOMETRIC_POLICY_EXPIRED,
    DEVICE_NOT_SECURE,
    OUT_OF_MEMORY,

    /** The vault file could not be written, so the previous state is unchanged. */
    STORAGE_FAILED,

    /** Biometrics were requested but no strong biometric is enrolled on the device. */
    BIOMETRIC_NOT_ENROLLED,

    /** The app was locked while the unlock ran, so the keys were zeroed again and no session opened. */
    LOCKED_DURING_UNLOCK,

    /** No password was given and the last password or recovery unlock is more than one minute old. */
    PASSWORD_REQUIRED,
}

public enum class PasswordRejection {
    TOO_SHORT,
    TOO_LONG,
    COMMON,
}
