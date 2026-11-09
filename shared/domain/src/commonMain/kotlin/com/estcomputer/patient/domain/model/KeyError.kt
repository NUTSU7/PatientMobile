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
}

public enum class PasswordRejection {
    TOO_SHORT,
    TOO_LONG,
    COMMON,
}
