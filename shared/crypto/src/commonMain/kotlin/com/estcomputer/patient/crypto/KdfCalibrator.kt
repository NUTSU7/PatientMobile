package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretChars

internal interface KdfCalibrator {
    /** Setup and password-change path: picks the parameters while deriving the key with the real password. */
    suspend fun calibrate(password: SecretChars, salt: ByteArray): CalibrationResult
}
