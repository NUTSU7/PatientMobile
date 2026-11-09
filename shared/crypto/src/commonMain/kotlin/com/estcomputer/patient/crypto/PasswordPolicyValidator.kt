package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.PasswordRejection
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.port.UnicodeNormalizer

/** NIST SP 800-63B policy: length in code points after NFKC and a common-password blocklist, no composition rules. */
internal class PasswordPolicyValidator(
    private val normalizer: UnicodeNormalizer,
    private val blocklist: PasswordBlocklist,
) {
    fun validate(password: SecretChars): PasswordCheck {
        // Cheap guard so a huge input is never copied into a String by the normaliser.
        if (password.size > MAX_RAW_CHARS) return PasswordCheck.Rejected(PasswordRejection.TOO_LONG)
        val normalized = password.read { normalizer.normalizeNfkc(it) }
        var isAccepted = false
        try {
            val rejection = rejectionFor(normalized)
            isAccepted = rejection == null
            return if (rejection == null) {
                PasswordCheck.Ok(SecretChars.wrap(normalized))
            } else {
                PasswordCheck.Rejected(rejection)
            }
        } finally {
            if (!isAccepted) normalized.fill('\u0000')
        }
    }

    private fun rejectionFor(normalized: CharArray): PasswordRejection? {
        val codePoints = countCodePoints(normalized)
        return when {
            codePoints < CryptoConstants.PASSWORD_MIN_CODE_POINTS -> PasswordRejection.TOO_SHORT
            codePoints > CryptoConstants.PASSWORD_MAX_CODE_POINTS -> PasswordRejection.TOO_LONG
            isCommon(normalized) -> PasswordRejection.COMMON
            else -> null
        }
    }

    private fun isCommon(normalized: CharArray): Boolean {
        val lower = CharArray(normalized.size) { normalized[it].lowercaseChar() }
        try {
            return blocklist.contains(lower)
        } finally {
            lower.fill('\u0000')
        }
    }

    private fun countCodePoints(chars: CharArray): Int {
        var count = 0
        var i = 0
        while (i < chars.size) {
            val isPair = chars[i].isHighSurrogate() && i + 1 < chars.size && chars[i + 1].isLowSurrogate()
            i += if (isPair) 2 else 1
            count++
        }
        return count
    }

    private companion object {
        const val RAW_LENGTH_FACTOR = 4
        const val MAX_RAW_CHARS = CryptoConstants.PASSWORD_MAX_CODE_POINTS * RAW_LENGTH_FACTOR
    }
}
