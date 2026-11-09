package com.estcomputer.patient.crypto

/** Common passwords, lowercase and NFKC-normalised, in UTF-16 code unit order. */
internal class PasswordBlocklist(entries: List<String>) {
    private val sorted = SortedStrings(entries)

    /** [lowercaseChars] must already be lowercase. */
    fun contains(lowercaseChars: CharArray): Boolean =
        lowercaseChars.isNotEmpty() &&
            sorted.indexOf(lowercaseChars, lowercaseChars.indices) != SortedStrings.NOT_FOUND
}
