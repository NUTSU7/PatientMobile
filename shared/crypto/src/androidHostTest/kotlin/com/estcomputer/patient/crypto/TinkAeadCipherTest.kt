package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TinkAeadCipherTest {
    private val cipher = TinkAeadCipher()
    private val key = SecretBytes.copyOf(ByteArray(CryptoConstants.KEY_SIZE_BYTES) { it.toByte() })
    private val message = "master key bytes".encodeToByteArray()

    private val vectors =
        Json.parseToJsonElement(TestData.text("aes_gcm_wycheproof.json")).jsonArray.map { it.jsonObject }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun JsonObject.hex(name: String) = getValue(name).jsonPrimitive.content.hexToBytes()

    private fun JsonObject.text(name: String) = getValue(name).jsonPrimitive.content

    private fun seal(aad: String = CryptoConstants.AAD_MK_PASSWORD) =
        cipher.seal(key, SecretBytes.copyOf(message), aad)

    private fun opened(result: OpenResult): ByteArray = assertIs<OpenResult.Ok>(result).plaintext.use { it.copyOf() }

    @Test
    fun open_wycheproofValidCases_decryptToMessage() {
        val valid = vectors.filter { it.text("result") == "valid" }

        assertEquals(VALID_CASES, valid.size)
        for (case in valid) {
            val result = cipher.open(SecretBytes.wrap(case.hex("key")), wire(case), case.hex("aad"))

            assertContentEquals(case.hex("msg"), opened(result), case.text("tcId"))
        }
    }

    @Test
    fun open_wycheproofInvalidCases_authFailed() {
        val invalid = vectors.filter { it.text("result") == "invalid" }

        assertEquals(INVALID_CASES, invalid.size)
        for (case in invalid) {
            val result = cipher.open(SecretBytes.wrap(case.hex("key")), wire(case), case.hex("aad"))

            assertEquals(OpenResult.AuthFailed, result, case.text("tcId"))
        }
    }

    @Test
    fun seal_thenOpen_returnsPlaintext() {
        assertContentEquals(message, opened(cipher.open(key, seal(), CryptoConstants.AAD_MK_PASSWORD)))
    }

    @Test
    fun seal_output_isNonceCiphertextTag() {
        val sealed = seal()

        assertEquals(CryptoConstants.GCM_NONCE_BYTES + message.size + CryptoConstants.GCM_TAG_BYTES, sealed.size)
    }

    @Test
    fun seal_output_opensWithPlainJcaAesGcm() {
        val sealed = seal()
        val jca = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = sealed.copyOf(CryptoConstants.GCM_NONCE_BYTES)
        val spec = GCMParameterSpec(CryptoConstants.GCM_TAG_BITS, iv)
        jca.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.read { it.copyOf() }, "AES"), spec)
        jca.updateAAD(CryptoConstants.AAD_MK_PASSWORD.encodeToByteArray())

        val nonceBytes = CryptoConstants.GCM_NONCE_BYTES
        val plaintext = jca.doFinal(sealed, nonceBytes, sealed.size - nonceBytes)

        assertContentEquals(message, plaintext)
    }

    @Test
    fun open_swappedAad_authFailed() {
        val sealed = seal(CryptoConstants.AAD_MK_PASSWORD)

        assertEquals(OpenResult.AuthFailed, cipher.open(key, sealed, CryptoConstants.AAD_MK_RECOVERY))
    }

    @Test
    fun open_wrongKey_authFailed() {
        val otherKey = SecretBytes.copyOf(ByteArray(CryptoConstants.KEY_SIZE_BYTES))

        assertEquals(OpenResult.AuthFailed, cipher.open(otherKey, seal(), CryptoConstants.AAD_MK_PASSWORD))
    }

    @Test
    fun open_anyFlippedBit_authFailed() {
        val sealed = seal()

        for (index in sealed.indices) {
            val tampered = sealed.copyOf().also { it[index] = (it[index].toInt() xor 1).toByte() }

            assertEquals(OpenResult.AuthFailed, cipher.open(key, tampered, CryptoConstants.AAD_MK_PASSWORD))
        }
    }

    @Test
    fun open_shorterThanNonceAndTag_authFailed() {
        for (size in 0 until CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.GCM_TAG_BYTES) {
            assertEquals(OpenResult.AuthFailed, cipher.open(key, ByteArray(size), CryptoConstants.AAD_MK_PASSWORD))
        }
    }

    @Test
    fun seal_manyTimes_neverRepeatsNonce() {
        val nonces = HashSet<String>()

        repeat(NONCE_SAMPLES) {
            nonces.add(seal().copyOf(CryptoConstants.GCM_NONCE_BYTES).toHexString())
        }

        assertEquals(NONCE_SAMPLES, nonces.size)
    }

    @Test
    fun seal_leavesKeyAndPlaintextIntact() {
        val plaintext = SecretBytes.copyOf(message)

        cipher.seal(key, plaintext, CryptoConstants.AAD_MK_PASSWORD)

        assertFalse(key.isZeroed)
        assertContentEquals(message, plaintext.read { it.copyOf() })
    }

    @Test
    fun open_result_isRedacted() {
        val result = cipher.open(key, seal(), CryptoConstants.AAD_MK_PASSWORD)

        assertEquals("Ok(***)", result.toString())
        assertTrue(result is OpenResult.Ok)
    }

    private fun wire(case: JsonObject) = case.hex("iv") + case.hex("ct") + case.hex("tag")

    private companion object {
        const val VALID_CASES = 39
        const val INVALID_CASES = 27
        const val NONCE_SAMPLES = 10_000
    }
}
