package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class BioPayloadTest {
    private val mkBytes = ByteArray(CryptoConstants.KEY_SIZE_BYTES) { (it + 1).toByte() }
    private val policy = BioPolicyData(bootCount = 42, passwordUnlockAt = Instant.parse("2026-11-01T10:20:30Z"))

    private fun payload(json: String): SecretBytes = SecretBytes.wrap(mkBytes + json.encodeToByteArray())

    @Test
    fun encode_thenDecode_roundTrips() {
        val encoded = BioPayload.encode(SecretBytes.copyOf(mkBytes), policy)

        val decoded = assertIs<BioPayloadDecodeResult.Ok>(BioPayload.decode(encoded))

        assertContentEquals(mkBytes, decoded.mk.read { it.copyOf() })
        assertEquals(policy, decoded.policy)
    }

    @Test
    fun encode_layout_isMkThenJson() {
        val bytes = BioPayload.encode(SecretBytes.copyOf(mkBytes), policy).read { it.copyOf() }

        assertContentEquals(mkBytes, bytes.copyOfRange(0, CryptoConstants.KEY_SIZE_BYTES))
        assertEquals(
            """{"boot_count":42,"password_unlock_at":"2026-11-01T10:20:30Z"}""",
            bytes.copyOfRange(CryptoConstants.KEY_SIZE_BYTES, bytes.size).decodeToString(),
        )
    }

    @Test
    fun decode_always_zeroesInput() {
        val input = payload("""{"boot_count":1,"password_unlock_at":"2026-11-01T10:20:30Z"}""")

        BioPayload.decode(input)

        assertTrue(input.isZeroed)
    }

    @Test
    fun decode_onlyMkOrShorter_isMalformed() {
        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(SecretBytes.copyOf(mkBytes)))
        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(SecretBytes.copyOf(ByteArray(5))))
        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(SecretBytes.copyOf(ByteArray(0))))
    }

    @Test
    fun decode_unknownKey_isMalformed() {
        val input = payload("""{"boot_count":1,"password_unlock_at":"2026-11-01T10:20:30Z","extra":1}""")

        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(input))
    }

    @Test
    fun decode_missingKey_isMalformed() {
        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(payload("""{"boot_count":1}""")))
    }

    @Test
    fun decode_malformedInstant_isMalformed() {
        assertEquals(
            BioPayloadDecodeResult.Malformed,
            BioPayload.decode(payload("""{"boot_count":1,"password_unlock_at":"yesterday"}""")),
        )
    }

    @Test
    fun decode_instantWithOffset_isMalformed() {
        assertEquals(
            BioPayloadDecodeResult.Malformed,
            BioPayload.decode(payload("""{"boot_count":1,"password_unlock_at":"2026-11-01T10:20:30+02:00"}""")),
        )
    }

    @Test
    fun decode_notJsonOrInvalidUtf8_isMalformed() {
        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(payload("not json")))
        val invalidUtf8 = SecretBytes.wrap(mkBytes + byteArrayOf(0xC3.toByte(), 0x28))
        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(invalidUtf8))
    }

    @Test
    fun decode_trailingData_isMalformed() {
        val input = payload("""{"boot_count":1,"password_unlock_at":"2026-11-01T10:20:30Z"}x""")

        assertEquals(BioPayloadDecodeResult.Malformed, BioPayload.decode(input))
    }

    @Test
    fun okResult_toString_isRedacted() {
        val ok = BioPayloadDecodeResult.Ok(SecretBytes.copyOf(mkBytes), policy)

        assertEquals("Ok(***)", ok.toString())
    }
}
