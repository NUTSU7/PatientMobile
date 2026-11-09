package com.estcomputer.patient.crypto

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BundledResourcesTest {
    @Test
    fun bip39Wordlist_bundled_has2048WordsAndReferenceHash() {
        val words = BundledTextResource.lines("bip39_english.txt")
        val digest = MessageDigest.getInstance("SHA-256").digest((words.joinToString("\n") + "\n").encodeToByteArray())

        assertEquals(2048, words.size)
        assertEquals(WORDLIST_SHA256, digest.joinToString("") { "%02x".format(it) })
    }

    @Test
    fun commonPasswords_bundled_isSortedAndLoadsIntoBlocklist() {
        val entries = BundledTextResource.lines("common_passwords.txt")

        assertTrue(entries.size >= MIN_ENTRIES)
        assertEquals(entries.sorted(), entries)
        assertTrue(entries.all { it.length >= CryptoConstants.PASSWORD_MIN_CODE_POINTS })
        val blocklist = PasswordBlocklist(entries)
        for (word in listOf("password", "parola123", "patient123", "pacient123", "parola1234", "parolamea")) {
            assertTrue(blocklist.contains(word.toCharArray()), word)
        }
    }

    @Test
    fun lines_missingResource_throwsWithoutName() {
        val error = assertFailsWith<java.io.FileNotFoundException> { BundledTextResource.lines("missing.txt") }

        assertEquals("bundled resource missing", error.message)
    }

    @Test
    fun javaUnicodeNormalizer_fullWidthAndDecomposition_normalises() {
        val normalizer = JavaUnicodeNormalizer()

        assertEquals("ABC1", String(normalizer.normalizeNfkc("ＡＢＣ１".toCharArray())))
        assertEquals("é", String(normalizer.normalizeNfkd("é".toCharArray())))
    }

    private companion object {
        const val WORDLIST_SHA256 = "2f5eed53a4727b4bf8880d8f3f199efc90e58503646d9ff8eff3a2ed3b24dbda"
        const val MIN_ENTRIES = 2_000
    }
}
