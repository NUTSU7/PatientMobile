package com.estcomputer.patient.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SortedStringsTest {
    private val strings = SortedStrings(listOf("apple", "apricot", "banana", "cherry"))

    @Test
    fun init_unsortedEntries_throws() {
        assertFailsWith<IllegalArgumentException> { SortedStrings(listOf("b", "a")) }
    }

    @Test
    fun init_duplicateEntries_throws() {
        assertFailsWith<IllegalArgumentException> { SortedStrings(listOf("a", "a")) }
    }

    @Test
    fun indexOf_slice_findsEntryInsideLargerArray() {
        val chars = "xxbananayy".toCharArray()

        assertEquals(2, strings.indexOf(chars, 2..7))
    }

    @Test
    fun indexOf_missingOrPrefixOnly_returnsNotFound() {
        assertEquals(SortedStrings.NOT_FOUND, strings.indexOf("app".toCharArray(), 0..2))
        assertEquals(SortedStrings.NOT_FOUND, strings.indexOf("bananas".toCharArray(), 0..6))
    }

    @Test
    fun withPrefix_matches_returnsInOrderUpToLimit() {
        assertEquals(listOf("apple", "apricot"), strings.withPrefix("ap".toCharArray(), 5))
        assertEquals(listOf("apple"), strings.withPrefix("ap".toCharArray(), 1))
        assertEquals(emptyList(), strings.withPrefix("z".toCharArray(), 5))
    }
}
