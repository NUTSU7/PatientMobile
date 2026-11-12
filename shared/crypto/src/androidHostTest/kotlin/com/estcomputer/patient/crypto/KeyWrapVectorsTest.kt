package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KeyWrapVectorsTest {
    private val aead = TinkAeadCipher()
    private val root = Json.parseToJsonElement(TestData.text("key_wrap_vectors.json")).jsonObject
    private val layers = root.getValue("layers").jsonArray.map { it.jsonObject }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun JsonObject.hex(name: String) = getValue(name).jsonPrimitive.content.hexToBytes()

    private fun JsonObject.aad() = hex("aad").decodeToString()

    @Test
    fun open_everyPersistedLayer_returnsTheMasterKey() {
        assertEquals(listOf("password", "recovery"), layers.map { it.getValue("name").jsonPrimitive.content })
        for (layer in layers) {
            val opened = aead.open(SecretBytes.wrap(layer.hex("key")), layer.hex("blob"), layer.aad())

            assertContentEquals(layer.hex("master_key"), assertIs<OpenResult.Ok>(opened).plaintext.use { it.copyOf() })
        }
    }

    @Test
    fun aadStrings_inTheVectors_matchThePersistedConstants() {
        val aads = layers.associate { it.getValue("name").jsonPrimitive.content to it.aad() }

        assertEquals(CryptoConstants.AAD_MK_PASSWORD, aads.getValue("password"))
        assertEquals(CryptoConstants.AAD_MK_RECOVERY, aads.getValue("recovery"))
    }

    @Test
    fun open_withAnotherLayersAad_failsAuthentication() {
        val password = layers.first()

        val opened = aead.open(SecretBytes.wrap(password.hex("key")), password.hex("blob"), CryptoConstants.AAD_MK_HW)

        assertEquals(OpenResult.AuthFailed, opened)
    }

    @Test
    fun recoveryKey_derivedFromTheSecret_matchesTheVectorKey() {
        val recovery = layers.single { it.getValue("name").jsonPrimitive.content == "recovery" }
        val secret = SecretBytes.wrap(root.hex("recovery_secret"))

        val key = KeyDerivation(HkdfSha256()).deriveRwk(secret)

        assertEquals(CryptoConstants.HKDF_INFO_RECOVERY_WRAP, root.hex("recovery_info").decodeToString())
        assertContentEquals(recovery.hex("key"), key.use { it.copyOf() })
    }

    @Test
    fun seal_ofTheMasterKey_hasTheSameLayoutAsTheVectorBlobAndOpens() {
        val layer = layers.first()
        val key = SecretBytes.wrap(layer.hex("key"))

        val sealed = aead.seal(key, SecretBytes.wrap(layer.hex("master_key")), layer.aad())

        assertEquals(layer.hex("blob").size, sealed.size)
        assertIs<OpenResult.Ok>(aead.open(key, sealed, layer.aad())).plaintext.zero()
    }
}
