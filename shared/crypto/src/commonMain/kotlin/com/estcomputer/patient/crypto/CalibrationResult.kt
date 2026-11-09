package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes

internal sealed interface CalibrationResult {
    /** [key] was derived with [params]; the caller zeroes it. */
    class Calibrated(val params: KdfParams, val key: SecretBytes) : CalibrationResult {
        override fun toString(): String = "Calibrated(***)"
    }

    data class Failed(val error: KeyError) : CalibrationResult
}
