package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.UnicodeNormalizer
import java.text.Normalizer

internal class JavaUnicodeNormalizer : UnicodeNormalizer {
    override fun normalizeNfkc(chars: CharArray): CharArray = normalize(chars, Normalizer.Form.NFKC)

    override fun normalizeNfkd(chars: CharArray): CharArray = normalize(chars, Normalizer.Form.NFKD)

    private fun normalize(chars: CharArray, form: Normalizer.Form): CharArray =
        Normalizer.normalize(String(chars), form).toCharArray()
}
