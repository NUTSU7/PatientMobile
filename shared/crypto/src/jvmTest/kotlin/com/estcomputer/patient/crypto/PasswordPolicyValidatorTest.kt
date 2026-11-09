package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.PasswordRejection
import com.estcomputer.patient.domain.model.SecretChars
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PasswordPolicyValidatorTest {
    private val validator = PasswordPolicyValidator(
        normalizer = JavaUnicodeNormalizer(),
        blocklist = PasswordBlocklist(listOf("123456789", "parola123", "password", "patient123")),
    )

    private fun check(text: String) = validator.validate(SecretChars.wrap(text.toCharArray()))

    private fun assertRejected(reason: PasswordRejection, text: String) {
        assertEquals(PasswordCheck.Rejected(reason), check(text))
    }

    private fun assertAccepted(text: String): String {
        val result = check(text)
        assertIs<PasswordCheck.Ok>(result)
        return result.normalized.use { String(it) }
    }

    @Test
    fun validate_sevenCodePoints_isTooShort() {
        assertRejected(PasswordRejection.TOO_SHORT, "abcdefg")
        assertAccepted("abcdefgh")
    }

    @Test
    fun validate_emoji_countsAsOneCodePointEach() {
        val sevenEmoji = "😀".repeat(7)

        assertRejected(PasswordRejection.TOO_SHORT, sevenEmoji)
        assertAccepted("😀".repeat(8))
    }

    @Test
    fun validate_combiningMarks_countAsOwnCodePoints() {
        // "q" plus 6 combining acute accents that have no precomposed form is 7 code points; 7 accents makes 8.
        assertRejected(PasswordRejection.TOO_SHORT, "q" + "́".repeat(6))
        assertAccepted("q" + "́".repeat(7))
    }

    @Test
    fun validate_length256And257_boundary() {
        assertAccepted("x7".repeat(128))
        assertRejected(PasswordRejection.TOO_LONG, "x7".repeat(128) + "y")
    }

    @Test
    fun validate_fullWidth_isNormalisedToAscii() {
        val fullWidth = "ＡＢＣｄｅｆ１２３"

        assertEquals("ABCdef123", assertAccepted(fullWidth))
    }

    @Test
    fun validate_commonPasswordAnyCase_isRejected() {
        assertRejected(PasswordRejection.COMMON, "PaSsWoRd")
        assertRejected(PasswordRejection.COMMON, "ＰＡＳＳＷＯＲＤ")
        assertRejected(PasswordRejection.COMMON, "Parola123")
    }

    @Test
    fun validate_appWord_isRejectedAsCommon() {
        assertRejected(PasswordRejection.COMMON, "Patient123")
    }

    @Test
    fun validate_patientAlone_isTooShortBeforeBlocklist() {
        assertRejected(PasswordRejection.TOO_SHORT, "patient")
    }

    @Test
    fun validate_hugeInput_isRejectedBeforeNormalisation() {
        assertRejected(PasswordRejection.TOO_LONG, "a".repeat(1_000_000))
    }

    @Test
    fun validate_turkishDottedCapitalI_doesNotCrash() {
        assertAccepted("İstanbul-2026")
    }

    @Test
    fun validate_sixtyFourCharPassphrase_isAccepted() {
        assertAccepted("correct horse battery staple correct horse battery staple 12345")
    }

    @Test
    fun validate_spacesOnlyInside_areAllowed() {
        assertAccepted("a b c d e f g h")
    }

    @Test
    fun passwordCheck_ok_toStringIsRedacted() {
        assertEquals("Ok(***)", check("abcdefgh").toString())
    }
}
