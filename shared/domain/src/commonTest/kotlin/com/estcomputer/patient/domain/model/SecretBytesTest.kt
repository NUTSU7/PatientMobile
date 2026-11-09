package com.estcomputer.patient.domain.model

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SecretBytesTest {

    @Test
    fun toString_always_isRedacted() {
        val secret = SecretBytes.copyOf(byteArrayOf(1, 2, 3))

        assertEquals("***", secret.toString())
    }

    @Test
    fun use_afterBlock_zeroesBytes() {
        val raw = byteArrayOf(1, 2, 3)
        val secret = SecretBytes.wrap(raw)

        val sum = secret.use { bytes -> bytes.sum() }

        assertEquals(6, sum)
        assertContentEquals(byteArrayOf(0, 0, 0), raw)
        assertTrue(secret.isZeroed)
    }

    @Test
    fun use_whenBlockThrows_stillZeroes() {
        val raw = byteArrayOf(9, 9)
        val secret = SecretBytes.wrap(raw)

        assertFailsWith<IllegalArgumentException> {
            secret.use { throw IllegalArgumentException("boom") }
        }

        assertContentEquals(byteArrayOf(0, 0), raw)
        assertTrue(secret.isZeroed)
    }

    @Test
    fun read_afterZero_throws() {
        val secret = SecretBytes.copyOf(byteArrayOf(1))
        secret.zero()

        val error = assertFailsWith<IllegalStateException> { secret.read { } }

        assertEquals("secret already cleared", error.message)
        assertFailsWith<IllegalStateException> { secret.use { } }
    }

    @Test
    fun read_beforeZero_doesNotClear() {
        val secret = SecretBytes.copyOf(byteArrayOf(5))

        secret.read { }

        assertFalse(secret.isZeroed)
    }

    @Test
    fun copyOf_doesNotAliasSource() {
        val source = byteArrayOf(1, 2, 3)
        val secret = SecretBytes.copyOf(source)

        secret.zero()

        assertContentEquals(byteArrayOf(1, 2, 3), source)
    }

    @Test
    fun equals_sameContent_isIdentityBased() {
        val a = SecretBytes.copyOf(byteArrayOf(1))
        val b = SecretBytes.copyOf(byteArrayOf(1))

        assertNotEquals(a, b)
        assertEquals(1, a.size)
    }
}
