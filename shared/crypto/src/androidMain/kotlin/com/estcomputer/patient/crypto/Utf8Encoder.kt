package com.estcomputer.patient.crypto

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CoderResult
import java.nio.charset.CodingErrorAction

internal object Utf8Encoder {
    /**
     * Exact UTF-8 bytes of [chars], without creating a String. The caller zeroes the result.
     * A lone surrogate throws `CharacterCodingException` instead of being replaced.
     */
    fun encode(chars: CharArray): ByteArray {
        val encoder = Charsets.UTF_8.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        // Sized for the worst case so the buffer never grows and leaves unzeroed copies behind.
        val buffer = ByteBuffer.allocate(chars.size * encoder.maxBytesPerChar().toInt())
        try {
            encoder.encode(CharBuffer.wrap(chars), buffer, true).throwUnlessDone()
            encoder.flush(buffer).throwUnlessDone()
            return buffer.array().copyOf(buffer.position())
        } finally {
            buffer.array().fill(0)
        }
    }

    private fun CoderResult.throwUnlessDone() {
        if (!isUnderflow) throwException()
    }
}
