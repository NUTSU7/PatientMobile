package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.port.Sha256
import com.estcomputer.patient.domain.port.UnicodeNormalizer

/**
 * BIP-39 mnemonic codec for 256-bit entropy. The recovery secret is the entropy itself, so there is no seed derivation.
 */
internal class Bip39Codec(
    private val sha256: Sha256,
    private val normalizer: UnicodeNormalizer,
    words: List<String>,
) {
    private val wordlist = Bip39Wordlist(words)

    fun encode(entropy: SecretBytes): SecretChars {
        require(entropy.size == ENTROPY_BYTES) { "entropy must be 32 bytes" }
        val packed = ByteArray(PACKED_BYTES)
        val indexes = IntArray(WORD_COUNT)
        try {
            entropy.read { bytes ->
                bytes.copyInto(packed)
                val digest = sha256.digest(bytes)
                packed[ENTROPY_BYTES] = digest[0]
                digest.fill(0)
            }
            for (i in indexes.indices) {
                indexes[i] = Bip39Bits.readIndex(packed, i)
            }
            return SecretChars.wrap(join(indexes))
        } finally {
            packed.fill(0)
            indexes.fill(0)
        }
    }

    fun decode(mnemonic: SecretChars): DecodeResult {
        val normalized = mnemonic.read { normalizer.normalizeNfkd(it) }
        val indexes = IntArray(WORD_COUNT)
        try {
            for (i in normalized.indices) {
                normalized[i] = normalized[i].lowercaseChar()
            }
            val tokens = tokenize(normalized)
            val unknown = if (tokens.size == WORD_COUNT) lookupAll(normalized, tokens, indexes) else NO_UNKNOWN
            return when {
                tokens.size != WORD_COUNT -> DecodeResult.WrongWordCount
                unknown != NO_UNKNOWN -> DecodeResult.UnknownWord(unknown)
                else -> verify(indexes)
            }
        } finally {
            normalized.fill('\u0000')
            indexes.fill(0)
        }
    }

    /** Returns at most [limit] wordlist entries starting with [prefix]. Never echoes user input. */
    fun suggest(prefix: CharArray, limit: Int): List<String> {
        if (prefix.isEmpty() || limit <= 0) return emptyList()
        val lower = CharArray(prefix.size) { prefix[it].lowercaseChar() }
        try {
            return wordlist.withPrefix(lower, limit)
        } finally {
            lower.fill('\u0000')
        }
    }

    private fun lookupAll(chars: CharArray, tokens: List<IntRange>, indexes: IntArray): Int {
        for ((position, range) in tokens.withIndex()) {
            val found = wordlist.indexOf(chars, range)
            if (found == Bip39Wordlist.NOT_FOUND) return position
            indexes[position] = found
        }
        return NO_UNKNOWN
    }

    private fun verify(indexes: IntArray): DecodeResult {
        val packed = ByteArray(PACKED_BYTES)
        val entropy = ByteArray(ENTROPY_BYTES)
        try {
            for (i in indexes.indices) {
                Bip39Bits.writeIndex(packed, i, indexes[i])
            }
            packed.copyInto(entropy, endIndex = ENTROPY_BYTES)
            val digest = sha256.digest(entropy)
            val isValid = digest[0] == packed[ENTROPY_BYTES]
            digest.fill(0)
            return if (isValid) DecodeResult.Ok(SecretBytes.copyOf(entropy)) else DecodeResult.ChecksumMismatch
        } finally {
            packed.fill(0)
            entropy.fill(0)
        }
    }

    private fun join(indexes: IntArray): CharArray {
        val out = CharArray(indexes.sumOf { wordlist[it].length } + (indexes.size - 1))
        var position = 0
        for ((i, wordIndex) in indexes.withIndex()) {
            if (i > 0) out[position++] = SEPARATOR
            for (c in wordlist[wordIndex]) out[position++] = c
        }
        return out
    }

    private fun tokenize(chars: CharArray): List<IntRange> {
        val tokens = ArrayList<IntRange>(WORD_COUNT)
        var start = -1
        for (i in chars.indices) {
            val isSpace = chars[i].isWhitespace()
            if (!isSpace && start < 0) start = i
            if (isSpace && start >= 0) {
                tokens.add(start until i)
                start = -1
            }
        }
        if (start >= 0) tokens.add(start until chars.size)
        return tokens
    }

    private companion object {
        const val WORD_COUNT = 24
        const val ENTROPY_BYTES = 32
        const val PACKED_BYTES = ENTROPY_BYTES + 1
        const val NO_UNKNOWN = -1
        const val SEPARATOR = ' '
    }
}
