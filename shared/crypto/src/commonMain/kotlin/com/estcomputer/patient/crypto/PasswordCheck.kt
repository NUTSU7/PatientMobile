package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.PasswordRejection
import com.estcomputer.patient.domain.model.SecretChars

internal sealed interface PasswordCheck {
    /** Holds the NFKC-normalised password. The caller zeroes [normalized]. */
    class Ok(val normalized: SecretChars) : PasswordCheck {
        override fun toString(): String = "Ok(***)"
    }

    data class Rejected(val reason: PasswordRejection) : PasswordCheck
}
