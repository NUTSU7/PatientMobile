package com.estcomputer.patient.crypto

import java.io.FileNotFoundException

/** Reads a UTF-8 text resource packaged with this module, one non-empty line per entry. */
internal object BundledTextResource {
    fun lines(name: String): List<String> {
        val stream = BundledTextResource::class.java.classLoader?.getResourceAsStream(name)
            ?: throw FileNotFoundException("bundled resource missing")
        return stream.bufferedReader().useLines { lines -> lines.filter { it.isNotEmpty() }.toList() }
    }
}
