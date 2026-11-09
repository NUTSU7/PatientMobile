package com.estcomputer.patient.crypto

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class JcaSha256Test {

    private val sha256 = JcaSha256()

    @Test
    fun digest_emptyInput_matchesNistVector() {
        val digest = sha256.digest(ByteArray(0))

        assertEquals(EMPTY_DIGEST, digest.toHex())
    }

    @Test
    fun digest_abc_matchesNistVector() {
        val digest = sha256.digest("abc".encodeToByteArray())

        assertEquals(ABC_DIGEST, digest.toHex())
    }

    @Test
    fun digest_sameInput_isDeterministicAnd32Bytes() {
        val input = "abc".encodeToByteArray()

        val first = sha256.digest(input)

        assertContentEquals(first, sha256.digest(input))
        assertEquals(CryptoConstants.KEY_SIZE_BYTES, first.size)
    }

    private fun ByteArray.toHex(): String = joinToString("") { byte ->
        (byte.toInt() and BYTE_MASK).toString(HEX_RADIX).padStart(HEX_DIGITS_PER_BYTE, '0')
    }

    private companion object {
        const val EMPTY_DIGEST = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        const val ABC_DIGEST = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        const val BYTE_MASK = 0xFF
        const val HEX_RADIX = 16
        const val HEX_DIGITS_PER_BYTE = 2
    }
}
