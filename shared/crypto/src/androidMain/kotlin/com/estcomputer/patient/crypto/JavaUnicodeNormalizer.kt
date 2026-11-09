package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.UnicodeNormalizer
import java.text.Normalizer

// Accepted risk: Normalizer works on String, so the input is briefly copied into an immutable string.
internal class JavaUnicodeNormalizer : UnicodeNormalizer {
    override fun normalizeNfkc(chars: CharArray): CharArray = normalize(chars, Normalizer.Form.NFKC)

    override fun normalizeNfkd(chars: CharArray): CharArray = normalize(chars, Normalizer.Form.NFKD)

    private fun normalize(chars: CharArray, form: Normalizer.Form): CharArray =
        Normalizer.normalize(String(chars), form).toCharArray()
}
