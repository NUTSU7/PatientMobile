package com.estcomputer.patient.crypto

/**
 * Read-only list of strings in UTF-16 code unit order. Lookups compare char slices against the entries,
 * so no `String` is built from user input.
 */
internal class SortedStrings(private val entries: List<String>) {
    init {
        require(entries.zipWithNext().all { (a, b) -> a < b }) { "entries must be sorted and unique" }
    }

    val size: Int get() = entries.size

    operator fun get(index: Int): String = entries[index]

    fun indexOf(chars: CharArray, range: IntRange): Int {
        var low = 0
        var high = entries.size - 1
        var found = NOT_FOUND
        while (low <= high && found == NOT_FOUND) {
            val mid = (low + high) ushr 1
            val order = compareSlice(chars, range.first, range.last + 1, entries[mid])
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
        while (index < entries.size && result.size < limit && entries[index].startsWithChars(prefix)) {
            result.add(entries[index])
            index++
        }
        return result
    }

    private fun firstAtOrAfter(prefix: CharArray): Int {
        var low = 0
        var high = entries.size
        while (low < high) {
            val mid = (low + high) ushr 1
            if (compareSlice(prefix, 0, prefix.size, entries[mid]) <= 0) high = mid else low = mid + 1
        }
        return low
    }

    private fun compareSlice(chars: CharArray, start: Int, end: Int, entry: String): Int {
        val length = minOf(end - start, entry.length)
        for (i in 0 until length) {
            val difference = chars[start + i] - entry[i]
            if (difference != 0) return difference
        }
        return (end - start) - entry.length
    }

    private fun String.startsWithChars(prefix: CharArray): Boolean =
        length >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    companion object {
        const val NOT_FOUND = -1
    }
}
