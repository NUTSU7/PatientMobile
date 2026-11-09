package com.estcomputer.patient.crypto

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Bip39WordlistTest {
    private val words = TestData.text(WORDLIST_FILE).lines().filter { it.isNotEmpty() }

    @Test
    fun wordlist_fileHash_matchesBitcoinBipsReference() {
        val digest = MessageDigest.getInstance("SHA-256").digest(TestData.bytes(WORDLIST_FILE))

        assertEquals(REFERENCE_SHA256, digest.joinToString("") { "%02x".format(it) })
    }

    @Test
    fun wordlist_content_has2048SortedUniqueWords() {
        assertEquals(2048, words.size)
        assertEquals(words.sorted(), words)
        assertEquals(words.size, words.toSet().size)
    }

    @Test
    fun wordlist_prefixes_areUniqueOverFourLetters() {
        val prefixes = words.map { it.take(4) }

        assertEquals(words.size, prefixes.toSet().size)
        assertTrue(words.all { it.length in 3..8 })
    }

    private companion object {
        const val WORDLIST_FILE = "bip39_english.txt"
        const val REFERENCE_SHA256 = "2f5eed53a4727b4bf8880d8f3f199efc90e58503646d9ff8eff3a2ed3b24dbda"
    }
}
