package com.estcomputer.patient.domain.model

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SecretCharsTest {

    @Test
    fun toString_always_isRedacted() {
        val secret = SecretChars.copyOf("hunter22".toCharArray())

        assertEquals("***", secret.toString())
    }

    @Test
    fun use_afterBlock_zeroesChars() {
        val raw = "abc".toCharArray()
        val secret = SecretChars.wrap(raw)

        val length = secret.use { chars -> chars.size }

        assertEquals(3, length)
        assertContentEquals(charArrayOf('\u0000', '\u0000', '\u0000'), raw)
        assertTrue(secret.isZeroed)
    }

    @Test
    fun use_whenBlockThrows_stillZeroes() {
        val raw = "ab".toCharArray()
        val secret = SecretChars.wrap(raw)

        assertFailsWith<IllegalArgumentException> {
            secret.use { throw IllegalArgumentException("boom") }
        }

        assertContentEquals(charArrayOf('\u0000', '\u0000'), raw)
        assertTrue(secret.isZeroed)
    }

    @Test
    fun read_afterZero_throws() {
        val secret = SecretChars.copyOf("a".toCharArray())
        secret.zero()

        val error = assertFailsWith<IllegalStateException> { secret.read { } }

        assertEquals("secret already cleared", error.message)
        assertFailsWith<IllegalStateException> { secret.use { } }
    }

    @Test
    fun read_beforeZero_doesNotClear() {
        val secret = SecretChars.copyOf("a".toCharArray())

        secret.read { }

        assertFalse(secret.isZeroed)
    }

    @Test
    fun copyOf_doesNotAliasSource() {
        val source = "abc".toCharArray()
        val secret = SecretChars.copyOf(source)

        secret.zero()

        assertContentEquals("abc".toCharArray(), source)
    }

    @Test
    fun equals_sameContent_isIdentityBased() {
        val a = SecretChars.copyOf("a".toCharArray())
        val b = SecretChars.copyOf("a".toCharArray())

        assertNotEquals(a, b)
        assertEquals(1, a.size)
    }
}
