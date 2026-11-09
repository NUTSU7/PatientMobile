package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretChars
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.Security
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame

class BouncyCastleArgon2idTest {
    private val argon2 = BouncyCastleArgon2id()

    private fun vectors(name: String) = Json.parseToJsonElement(TestData.text(name)).jsonArray.map { it.jsonObject }

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun JsonObject.hex(key: String) = getValue(key).jsonPrimitive.content.hexToBytes()

    private fun JsonObject.int(key: String) = getValue(key).jsonPrimitive.int

    private fun JsonObject.params() = KdfParams(hex("salt"), int("memory_kib"), int("iterations"), int("parallelism"))

    private fun derivedKey(password: SecretChars, params: KdfParams): ByteArray =
        assertIs<KdfResult.Derived>(argon2.derive(password, params)).key.use { it.copyOf() }

    @Test
    fun compute_rfc9106Vector_matchesPublishedTag() {
        val case = vectors("argon2id_rfc9106.json").single()
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withSalt(case.hex("salt"))
            .withSecret(case.hex("secret"))
            .withAdditional(case.hex("additional"))
            .withMemoryAsKB(case.int("memory_kib"))
            .withIterations(case.int("iterations"))
            .withParallelism(case.int("parallelism"))

        val tag = argon2.compute(case.hex("password"), builder, case.hex("tag").size)

        assertContentEquals(case.hex("tag"), tag)
    }

    @Test
    fun derive_productionParams_matchesIndependentReference() {
        val case = vectors("argon2id_app_vectors.json").single()
        val password = SecretChars.wrap(case.hex("password_utf8").decodeToString().toCharArray())

        assertEquals(CryptoConstants.ARGON2_MEMORY_KIB, case.int("memory_kib"))
        assertEquals(CryptoConstants.ARGON2_ITERATIONS, case.int("iterations"))
        assertEquals(CryptoConstants.ARGON2_PARALLELISM, case.int("parallelism"))
        assertContentEquals(case.hex("tag"), derivedKey(password, case.params()))
    }

    @Test
    fun derive_throughEncoder_equalsDirectBytes() {
        val salt = ByteArray(CryptoConstants.SALT_BYTES) { it.toByte() }
        val params = KdfParams(salt, CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB, 1, 1)
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withSalt(salt)
            .withMemoryAsKB(params.memoryKib)
            .withIterations(params.iterations)
            .withParallelism(params.parallelism)

        val direct = argon2.compute("password".encodeToByteArray(), builder, CryptoConstants.KEY_SIZE_BYTES)

        assertContentEquals(direct, derivedKey(SecretChars.wrap("password".toCharArray()), params))
    }

    @Test
    fun derive_leavesPasswordAndSaltIntact() {
        val salt = ByteArray(CryptoConstants.SALT_BYTES) { 7 }
        val password = SecretChars.wrap("password".toCharArray())

        derivedKey(password, KdfParams(salt, CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB, 1, 1))

        assertFalse(password.isZeroed)
        assertContentEquals(ByteArray(CryptoConstants.SALT_BYTES) { 7 }, salt)
    }

    @Test
    fun derive_neverRegistersSecurityProvider() {
        val before = Security.getProviders().toList()
        val bcBefore = Security.getProvider(BC_PROVIDER)

        derivedKey(SecretChars.wrap("password".toCharArray()), KdfParams(ByteArray(16), 32_768, 1, 1))

        assertEquals(before, Security.getProviders().toList())
        assertSame(bcBefore, Security.getProvider(BC_PROVIDER))
    }

    private companion object {
        const val BC_PROVIDER = "BC"
    }
}
