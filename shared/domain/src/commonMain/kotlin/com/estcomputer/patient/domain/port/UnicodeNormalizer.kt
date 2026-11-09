package com.estcomputer.patient.domain.port

/** Platform Unicode normalisation (AGENTS §8). The caller zeroes both the input and the returned array. */
public interface UnicodeNormalizer {
    public fun normalizeNfkc(chars: CharArray): CharArray

    public fun normalizeNfkd(chars: CharArray): CharArray
}
