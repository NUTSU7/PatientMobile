package com.estcomputer.patient.domain.model

/**
 * Owns a char array holding a password or recovery words.
 * `toString()` is redacted and equality is by identity.
 */
public class SecretChars private constructor(private val chars: CharArray) {

    public var isZeroed: Boolean = false
        private set

    public val size: Int = chars.size

    /** Borrows the chars without clearing them. The array must not escape [block]. */
    public fun <R> read(block: (CharArray) -> R): R {
        check(!isZeroed) { CLEARED_MESSAGE }
        return block(chars)
    }

    /** Borrows the chars and zeroes them afterwards, even when [block] throws. */
    public fun <R> use(block: (CharArray) -> R): R {
        try {
            return read(block)
        } finally {
            zero()
        }
    }

    public fun zero() {
        chars.fill('\u0000')
        isZeroed = true
    }

    override fun toString(): String = "***"

    public companion object {
        internal const val CLEARED_MESSAGE: String = "secret already cleared"

        /** Takes ownership of [chars] without copying; the caller must not keep using the array. */
        public fun wrap(chars: CharArray): SecretChars = SecretChars(chars)

        public fun copyOf(source: CharArray): SecretChars = SecretChars(source.copyOf())
    }
}
