package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HkdfSha256Test {
    private val hkdf = HkdfSha256()

    private val vectors = Json.parseToJsonElement(TestData.text("hkdf_vectors.json")).jsonArray.map { it.jsonObject }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun field(vector: Map<String, JsonElement>, key: String) =
        vector.getValue(key).jsonPrimitive.content

    @Test
    fun compute_rfc5869Cases_matchPublishedOutput() {
        val rfcCases = vectors.filter { field(it, "name").startsWith("rfc5869_") }

        assertEquals(3, rfcCases.size)
        for (case in rfcCases) {
            val okm = hkdf.compute(
                ikm = field(case, "ikm").hexToBytes(),
                salt = field(case, "salt").hexToBytes(),
                info = field(case, "info").hexToBytes(),
                length = case.getValue("length").jsonPrimitive.int,
            )

            assertContentEquals(field(case, "okm").hexToBytes(), okm, field(case, "name"))
        }
    }

    @Test
    fun derive_appInfoStrings_matchIndependentReference() {
        val appCases = vectors.filter { field(it, "name").startsWith("app:") }

        assertEquals(5, appCases.size)
        for (case in appCases) {
            val info = field(case, "name").removePrefix("app:")
            val ikm = SecretBytes.wrap(field(case, "ikm").hexToBytes())

            val okm = hkdf.derive(ikm, info)

            assertContentEquals(field(case, "okm").hexToBytes(), okm.use { it.copyOf() }, info)
        }
    }

    @Test
    fun derive_appCaseInfoField_equalsNameBytes() {
        for (case in vectors.filter { field(it, "name").startsWith("app:") }) {
            val expected = field(case, "name").removePrefix("app:").encodeToByteArray()

            assertContentEquals(expected, field(case, "info").hexToBytes())
        }
    }

    @Test
    fun derive_differentInfo_givesDifferentKeys() {
        val ikm = SecretBytes.copyOf(ByteArray(32) { it.toByte() })

        val dbk = hkdf.derive(ikm, "patient/db/v1").use { it.copyOf() }
        val fkek = hkdf.derive(ikm, "patient/file-kek/v1").use { it.copyOf() }

        assertEquals(32, dbk.size)
        assertTrue(!dbk.contentEquals(fkek))
    }
}
