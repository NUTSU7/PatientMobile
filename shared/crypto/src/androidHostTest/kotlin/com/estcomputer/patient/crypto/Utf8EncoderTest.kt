package com.estcomputer.patient.crypto

import java.nio.charset.CharacterCodingException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class Utf8EncoderTest {
    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun assertEncodes(text: String, expectedHex: String) {
        assertContentEquals(expectedHex.hexToBytes(), Utf8Encoder.encode(text.toCharArray()))
    }

    @Test
    fun encode_ascii_exactBytes() {
        assertEncodes("password", "70617373776f7264")
    }

    @Test
    fun encode_romanianDiacritics_exactBytes() {
        assertEncodes("șțăîâ", "c899c89bc483c3aec3a2")
    }

    @Test
    fun encode_emojiSurrogatePair_fourBytes() {
        assertEncodes("🩺", "f09fa9ba")
    }

    @Test
    fun encode_fullWidth_threeBytesPerChar() {
        assertEncodes("ＡＢ", "efbca1efbca2")
    }

    @Test
    fun encode_empty_emptyArray() {
        assertEncodes("", "")
    }

    @Test
    fun encode_longNonAscii_exactLength() {
        val text = "ș".repeat(1000) + "🩺".repeat(500)

        assertContentEquals(text.encodeToByteArray(), Utf8Encoder.encode(text.toCharArray()))
    }

    @Test
    fun encode_loneHighSurrogate_rejected() {
        assertFailsWith<CharacterCodingException> { Utf8Encoder.encode(charArrayOf('a', '\uD83E')) }
    }

    @Test
    fun encode_loneLowSurrogate_rejected() {
        assertFailsWith<CharacterCodingException> { Utf8Encoder.encode(charArrayOf('\uDE7A', 'a')) }
    }
}
