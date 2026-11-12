package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError

internal fun KeystoreError.toKeyError(): KeyError =
    when (this) {
        KeystoreError.HW_KEY_MISSING, KeystoreError.RECOVERY_REQUIRED -> KeyError.RECOVERY_REQUIRED
        KeystoreError.HW_KEY_INVALIDATED -> KeyError.HW_KEY_INVALIDATED
        KeystoreError.DEVICE_LOCKED -> KeyError.DEVICE_LOCKED
    }
