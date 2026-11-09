package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TinkX25519Test {
    private val x25519 = TinkX25519()
    private val deriver = ExportKeyDeriver(KeyDerivation(HkdfSha256()), x25519)

    private val vectors =
        Json.parseToJsonElement(TestData.text("export_key_vectors.json")).jsonArray.map { it.jsonObject }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun JsonObject.hex(name: String) = getValue(name).jsonPrimitive.content.hexToBytes()

    private fun JsonObject.name() = getValue("name").jsonPrimitive.content

    @Test
    fun publicKey_rfc7748Section61_matchesAliceAndBob() {
        val rfcCases = vectors.filter { it.name().startsWith("rfc7748_") }

        assertEquals(2, rfcCases.size)
        for (case in rfcCases) {
            val key = x25519.publicKey(SecretBytes.wrap(case.hex("private")))

            assertContentEquals(case.hex("public"), key, case.name())
        }
    }

    @Test
    fun publicKey_exportDerivation_matchesIndependentReference() {
        val case = vectors.single { it.name().startsWith("app:") }

        val key = deriver.publicKey(SecretBytes.wrap(case.hex("recovery_secret")))

        assertContentEquals(case.hex("public"), key)
    }

    @Test
    fun publicKey_exportDerivation_coversUnclampedPrivateKey() {
        val case = vectors.single { it.name().startsWith("app:") }

        assertFalse(case.getValue("is_private_clamped").jsonPrimitive.boolean)
    }

    @Test
    fun publicKey_exportDerivation_isThirtyTwoBytes() {
        val key = deriver.publicKey(SecretBytes.wrap(ByteArray(CryptoConstants.KEY_SIZE_BYTES)))

        assertEquals(CryptoConstants.KEY_SIZE_BYTES, key.size)
    }
}
