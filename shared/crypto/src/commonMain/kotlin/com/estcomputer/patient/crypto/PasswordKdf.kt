package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretChars

internal interface PasswordKdf {
    /** Unlock path: uses the stored [params] as they are and never recalibrates. */
    suspend fun derive(password: SecretChars, params: KdfParams): KdfResult
}
