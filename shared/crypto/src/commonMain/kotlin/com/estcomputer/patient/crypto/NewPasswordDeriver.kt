package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.PasswordRejection
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars

internal sealed interface NewPasswordResult {
    /** [key] was derived with [params] under a fresh salt; the caller zeroes it. */
    class Ready(val params: KdfParams, val key: SecretBytes) : NewPasswordResult {
        override fun toString(): String = "Ready(***)"
    }

    data class Rejected(val reason: PasswordRejection) : NewPasswordResult

    data class Failed(val error: KeyError) : NewPasswordResult
}

/** Policy check, then a fresh salt and a calibrated password key, for recovery and password change. */
internal class NewPasswordDeriver(
    private val validator: PasswordPolicyValidator,
    private val calibrator: KdfCalibrator,
    private val random: SecureRandomSource,
) {
    suspend fun derive(password: SecretChars): NewPasswordResult =
        when (val checked = validator.validate(password)) {
            is PasswordCheck.Rejected -> NewPasswordResult.Rejected(checked.reason)
            is PasswordCheck.Ok ->
                try {
                    calibrate(checked.normalized)
                } finally {
                    checked.normalized.zero()
                }
        }

    private suspend fun calibrate(normalized: SecretChars): NewPasswordResult =
        when (val calibrated = calibrator.calibrate(normalized, random.nextBytes(CryptoConstants.SALT_BYTES))) {
            is CalibrationResult.Failed -> NewPasswordResult.Failed(calibrated.error)
            is CalibrationResult.Calibrated -> NewPasswordResult.Ready(calibrated.params, calibrated.key)
        }
}
