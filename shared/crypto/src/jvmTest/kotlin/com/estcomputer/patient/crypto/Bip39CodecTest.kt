package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class Bip39CodecTest {
    private val codec = Bip39Codec(
        sha256 = JcaSha256(),
        normalizer = JavaUnicodeNormalizer(),
        words = TestData.text("bip39_english.txt").lines().filter { it.isNotEmpty() },
    )

    private val vectors: List<Pair<ByteArray, String>> =
        Json.parseToJsonElement(TestData.text("bip39_vectors.json")).jsonArray.map {
            val row = it.jsonObject
            row.getValue("entropy").jsonPrimitive.content.hexToBytes() to
                row.getValue("mnemonic").jsonPrimitive.content
        }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun chars(text: String) = SecretChars.wrap(text.toCharArray())

    private fun decodeOk(text: String): ByteArray {
        val result = codec.decode(chars(text))
        assertIs<DecodeResult.Ok>(result)
        return result.entropy.use { it.copyOf() }
    }

    @Test
    fun vectors_file_holdsEightPairs() {
        assertEquals(8, vectors.size)
    }

    @Test
    fun encode_trezorVectors_matchMnemonics() {
        for ((entropy, mnemonic) in vectors) {
            val words = codec.encode(SecretBytes.copyOf(entropy))

            assertEquals(mnemonic, words.use { String(it) })
        }
    }

    @Test
    fun decode_trezorVectors_matchEntropy() {
        for ((entropy, mnemonic) in vectors) {
            assertContentEquals(entropy, decodeOk(mnemonic))
        }
    }

    @Test
    fun decode_afterEncode_roundTrips() {
        val entropy = ByteArray(32) { (it * 7 + 3).toByte() }
        val words = codec.encode(SecretBytes.copyOf(entropy))

        val decoded = words.use { codec.decode(SecretChars.copyOf(it)) }

        assertIs<DecodeResult.Ok>(decoded)
        assertContentEquals(entropy, decoded.entropy.use { it.copyOf() })
    }

    @Test
    fun decode_oneWordReplaced_returnsChecksumMismatch() {
        val words = vectors[0].second.split(" ").toMutableList()
        words[23] = "zoo"

        assertEquals(DecodeResult.ChecksumMismatch, codec.decode(chars(words.joinToString(" "))))
    }

    @Test
    fun decode_wrongWordCount_returnsWrongWordCount() {
        val words = vectors[0].second.split(" ")

        assertEquals(DecodeResult.WrongWordCount, codec.decode(chars(words.take(23).joinToString(" "))))
        assertEquals(DecodeResult.WrongWordCount, codec.decode(chars((words + "abandon").joinToString(" "))))
    }

    @Test
    fun decode_unknownWord_returnsItsIndex() {
        val words = vectors[0].second.split(" ").toMutableList()
        words[5] = "notaword"

        assertEquals(DecodeResult.UnknownWord(5), codec.decode(chars(words.joinToString(" "))))
    }

    @Test
    fun decode_uppercaseAndExtraSpaces_isAccepted() {
        val (entropy, mnemonic) = vectors[1]
        val messy = "  " + mnemonic.uppercase().replace(" ", "   ") + " \n"

        assertContentEquals(entropy, decodeOk(messy))
    }

    @Test
    fun decode_fullWidthLetters_isAcceptedAfterNormalisation() {
        val (entropy, mnemonic) = vectors[1]
        val fullWidth = mnemonic.map { if (it == ' ') it else (it.code + FULL_WIDTH_OFFSET).toChar() }.joinToString("")

        assertContentEquals(entropy, decodeOk(fullWidth))
    }

    @Test
    fun encode_wrongEntropySize_throwsWithoutData() {
        val error = assertFailsWith<IllegalArgumentException> { codec.encode(SecretBytes.copyOf(ByteArray(16))) }

        assertEquals("entropy must be 32 bytes", error.message)
    }

    @Test
    fun decodeResult_ok_toStringIsRedacted() {
        val result = codec.decode(chars(vectors[0].second))

        assertEquals("Ok(***)", result.toString())
    }

    @Test
    fun suggest_prefix_returnsMatchingWordsUpToLimit() {
        assertEquals(listOf("zebra", "zero", "zone", "zoo"), codec.suggest("Z".toCharArray(), 10))
        assertEquals(listOf("abandon", "ability"), codec.suggest("ab".toCharArray(), 2))
        assertEquals(emptyList(), codec.suggest("qqq".toCharArray(), 5))
        assertEquals(emptyList(), codec.suggest(CharArray(0), 5))
        assertTrue(codec.suggest("zo".toCharArray(), 10).all { it.startsWith("zo") })
    }

    private companion object {
        const val FULL_WIDTH_OFFSET = 0xFEE0
    }
}
