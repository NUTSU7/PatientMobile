package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.text.CharacterCodingException
import kotlin.time.Instant

/** Policy data sealed next to the MK in the biometric blob (D5). */
internal data class BioPolicyData(
    /** A value that changes on every reboot. */
    val bootCount: Int,
    val passwordUnlockAt: Instant,
)

internal sealed interface BioPayloadDecodeResult {
    /** The caller zeroes [mk]. */
    class Ok(val mk: SecretBytes, val policy: BioPolicyData) : BioPayloadDecodeResult {
        override fun toString(): String = "Ok(***)"
    }

    data object Malformed : BioPayloadDecodeResult
}

/** Layout: `MK (32) ‖ UTF-8 JSON {"boot_count":…, "password_unlock_at":"…Z"}`. */
internal object BioPayload {
    /** Returns a new secret holding the layout; the caller zeroes it and keeps ownership of [mk]. */
    fun encode(mk: SecretBytes, policy: BioPolicyData): SecretBytes {
        val json = JSON.encodeToString(
            Wire.serializer(),
            Wire(policy.bootCount, policy.passwordUnlockAt.toString()),
        ).encodeToByteArray()
        return SecretBytes.wrap(mk.read { it.copyOf() + json }.also { json.fill(0) })
    }

    /** Consumes [payload]: it is zeroed on every path. */
    fun decode(payload: SecretBytes): BioPayloadDecodeResult =
        payload.use { bytes ->
            if (bytes.size <= CryptoConstants.KEY_SIZE_BYTES) return@use BioPayloadDecodeResult.Malformed
            val policy = parsePolicy(bytes.copyOfRange(CryptoConstants.KEY_SIZE_BYTES, bytes.size))
                ?: return@use BioPayloadDecodeResult.Malformed
            BioPayloadDecodeResult.Ok(SecretBytes.wrap(bytes.copyOfRange(0, CryptoConstants.KEY_SIZE_BYTES)), policy)
        }

    private fun parsePolicy(json: ByteArray): BioPolicyData? =
        try {
            val wire = JSON.decodeFromString(Wire.serializer(), json.decodeToString(throwOnInvalidSequence = true))
            if (wire.passwordUnlockAt.endsWith(UTC_SUFFIX)) {
                BioPolicyData(wire.bootCount, Instant.parse(wire.passwordUnlockAt))
            } else {
                null
            }
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: CharacterCodingException) {
            null
        }

    @Serializable
    private class Wire(
        @SerialName("boot_count") val bootCount: Int,
        @SerialName("password_unlock_at") val passwordUnlockAt: String,
    )

    private const val UTC_SUFFIX = "Z"

    private val JSON = Json {
        ignoreUnknownKeys = false
        explicitNulls = true
        encodeDefaults = true
    }
}
