package com.estcomputer.patient.crypto

internal class Bip39Wordlist(private val words: List<String>) {
    init {
        require(words.size == SIZE) { "invalid wordlist size" }
    }

    operator fun get(index: Int): String = words[index]

    /** Binary search of `chars[range]` against the list without building a `String` from user input. */
    fun indexOf(chars: CharArray, range: IntRange): Int {
        var low = 0
        var high = words.size - 1
        var found = NOT_FOUND
        while (low <= high && found == NOT_FOUND) {
            val mid = (low + high) ushr 1
            val order = compareSlice(chars, range.first, range.last + 1, words[mid])
            when {
                order == 0 -> found = mid
                order < 0 -> high = mid - 1
                else -> low = mid + 1
            }
        }
        return found
    }

    fun withPrefix(prefix: CharArray, limit: Int): List<String> {
        val result = ArrayList<String>(limit)
        var index = firstAtOrAfter(prefix)
        while (index < words.size && result.size < limit && words[index].startsWithChars(prefix)) {
            result.add(words[index])
            index++
        }
        return result
    }

    private fun firstAtOrAfter(prefix: CharArray): Int {
        var low = 0
        var high = words.size
        while (low < high) {
            val mid = (low + high) ushr 1
            if (compareSlice(prefix, 0, prefix.size, words[mid]) <= 0) high = mid else low = mid + 1
        }
        return low
    }

    private fun compareSlice(chars: CharArray, start: Int, end: Int, word: String): Int {
        val length = minOf(end - start, word.length)
        for (i in 0 until length) {
            val difference = chars[start + i] - word[i]
            if (difference != 0) return difference
        }
        return (end - start) - word.length
    }

    private fun String.startsWithChars(prefix: CharArray): Boolean =
        length >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    companion object {
        const val SIZE = 2048
        const val NOT_FOUND = -1
    }
}
