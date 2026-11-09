package com.estcomputer.patient.domain.model

/**
 * Owns a byte array holding key material or another secret.
 * `toString()` is redacted and equality is by identity, so a secret never leaks through logs or value-keyed maps.
 */
public class SecretBytes private constructor(private val bytes: ByteArray) {

    public var isZeroed: Boolean = false
        private set

    public val size: Int = bytes.size

    /** Borrows the bytes without clearing them. The array must not escape [block]. */
    public fun <R> read(block: (ByteArray) -> R): R {
        check(!isZeroed) { CLEARED_MESSAGE }
        return block(bytes)
    }

    /** Borrows the bytes and zeroes them afterwards, even when [block] throws. */
    public fun <R> use(block: (ByteArray) -> R): R {
        try {
            return read(block)
        } finally {
            zero()
        }
    }

    public fun zero() {
        bytes.fill(0)
        isZeroed = true
    }

    override fun toString(): String = "***"

    public companion object {
        internal const val CLEARED_MESSAGE: String = "secret already cleared"

        /** Takes ownership of [bytes] without copying; the caller must not keep using the array. */
        public fun wrap(bytes: ByteArray): SecretBytes = SecretBytes(bytes)

        public fun copyOf(source: ByteArray): SecretBytes = SecretBytes(source.copyOf())
    }
}
