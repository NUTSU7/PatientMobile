package com.estcomputer.patient.crypto

/** Packs and unpacks 11-bit word indexes in a big-endian bit string, as BIP-39 defines. */
internal object Bip39Bits {
    const val BITS_PER_WORD = 11
    private const val BYTE_SHIFT = 3
    private const val BIT_MASK = 7
    private const val LAST_BIT = 7

    fun readIndex(packed: ByteArray, wordPosition: Int): Int {
        var value = 0
        for (bit in 0 until BITS_PER_WORD) {
            val position = wordPosition * BITS_PER_WORD + bit
            val isSet = (packed[position ushr BYTE_SHIFT].toInt() ushr (LAST_BIT - (position and BIT_MASK))) and 1
            value = (value shl 1) or isSet
        }
        return value
    }

    fun writeIndex(packed: ByteArray, wordPosition: Int, index: Int) {
        for (bit in 0 until BITS_PER_WORD) {
            if ((index ushr (BITS_PER_WORD - 1 - bit)) and 1 == 1) {
                val position = wordPosition * BITS_PER_WORD + bit
                val byte = position ushr BYTE_SHIFT
                packed[byte] = (packed[byte].toInt() or (1 shl (LAST_BIT - (position and BIT_MASK)))).toByte()
            }
        }
    }
}
