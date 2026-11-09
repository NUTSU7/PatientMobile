package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class VaultCodecTest {
    private val codec = VaultCodec()
    private val golden = TestData.text("vault_v1_golden.json")

    private val corrupt = VaultDecodeResult.Failed(KeyError.VAULT_CORRUPT)

    private fun decoded(text: String = golden): VaultFile =
        assertIs<VaultDecodeResult.Ok>(codec.decode(text.encodeToByteArray())).vault

    private fun assertCorrupt(text: String) = assertEquals(corrupt, codec.decode(text.encodeToByteArray()))

    private fun mutated(from: String, to: String): String {
        assertTrue(golden.contains(from), from)
        return golden.replace(from, to)
    }

    private fun base64Of(size: Int) = Base64.encode(ByteArray(size))

    @Test
    fun decode_golden_readsDocumentedFields() {
        val vault = decoded()

        assertEquals(1, vault.format)
        assertEquals("2026-11-06T10:00:00Z", vault.createdAt)
        assertEquals("argon2id", vault.kdf.alg)
        assertContentEquals(ByteArray(CryptoConstants.SALT_BYTES) { it.toByte() }, vault.kdf.salt)
        assertEquals(CryptoConstants.ARGON2_MEMORY_KIB, vault.kdf.memoryKib)
        assertEquals(CryptoConstants.HW_KEY_ALIAS, vault.hwKeyAlias)
        assertEquals(88, vault.wrappedMkPassword.size)
        assertEquals(60, vault.wrappedMkRecovery.size)
        assertEquals(null, vault.wrappedMkBiometric)
        assertEquals(null, vault.bioKeyAlias)
        assertEquals(CryptoConstants.KEY_SIZE_BYTES, vault.exportPublicKey.size)
    }

    @Test
    fun encode_decodedGolden_isByteIdentical() {
        assertEquals(golden, codec.encode(decoded()).decodeToString())
    }

    @Test
    fun encode_nullBiometricFields_writtenAsNull() {
        val text = codec.encode(decoded()).decodeToString()

        assertTrue(text.contains("\"wrapped_mk_biometric\":null"))
        assertTrue(text.contains("\"bio_key_alias\":null"))
    }

    @Test
    fun encode_withBiometric_roundTrips() {
        val withBio = decoded().copy(
            wrappedMkBiometric = ByteArray(BIO_BLOB_BYTES) { 9 },
            bioKeyAlias = CryptoConstants.BIO_KEY_ALIAS,
            failedAttempts = 3,
            wipeAfterFailedAttempts = CryptoConstants.WIPE_AFTER_MIN,
            lockoutUntilElapsed = 123_456_789_012,
            lockoutBootCount = 42,
        )

        val back = decoded(codec.encode(withBio).decodeToString())

        assertContentEquals(withBio.wrappedMkBiometric, back.wrappedMkBiometric)
        assertEquals(CryptoConstants.BIO_KEY_ALIAS, back.bioKeyAlias)
        assertEquals(3, back.failedAttempts)
        assertEquals(CryptoConstants.WIPE_AFTER_MIN, back.wipeAfterFailedAttempts)
        assertEquals(123_456_789_012, back.lockoutUntilElapsed)
        assertEquals(42, back.lockoutBootCount)
    }

    @Test
    fun decode_unknownFormat_vaultCorrupt() {
        assertCorrupt(mutated("\"format\":1", "\"format\":2"))
    }

    @Test
    fun decode_unknownKey_vaultCorrupt() {
        assertCorrupt(mutated("\"format\":1,", "\"format\":1,\"extra\":true,"))
    }

    @Test
    fun decode_missingKey_vaultCorrupt() {
        assertCorrupt(mutated("\"hw_key_alias\":\"patient_hw_v1\",", ""))
    }

    @Test
    fun decode_wrongKdfAlgorithm_vaultCorrupt() {
        assertCorrupt(mutated("argon2id", "argon2i"))
    }

    @Test
    fun decode_kdfParamsOutOfBounds_vaultCorrupt() {
        assertCorrupt(mutated("\"memory_kib\":65536", "\"memory_kib\":16384"))
        assertCorrupt(mutated("\"iterations\":3", "\"iterations\":0"))
        assertCorrupt(mutated("\"iterations\":3", "\"iterations\":11"))
        assertCorrupt(mutated("\"parallelism\":1", "\"parallelism\":2"))
    }

    @Test
    fun decode_wrongSaltSize_vaultCorrupt() {
        val salt = Base64.encode(ByteArray(CryptoConstants.SALT_BYTES) { it.toByte() })

        assertCorrupt(mutated(salt, base64Of(CryptoConstants.SALT_BYTES - 1)))
        assertCorrupt(mutated(salt, base64Of(CryptoConstants.SALT_BYTES + 1)))
    }

    @Test
    fun decode_wrongWrappedLengths_vaultCorrupt() {
        val vault = decoded()
        val password = Base64.encode(vault.wrappedMkPassword)
        val recovery = Base64.encode(vault.wrappedMkRecovery)

        assertCorrupt(mutated(password, base64Of(87)))
        assertCorrupt(mutated(password, base64Of(89)))
        assertCorrupt(mutated(recovery, base64Of(59)))
        assertCorrupt(mutated(recovery, base64Of(61)))
    }

    @Test
    fun decode_wrongExportKeySize_vaultCorrupt() {
        val key = Base64.encode(decoded().exportPublicKey)

        assertCorrupt(mutated(key, base64Of(31)))
        assertCorrupt(mutated(key, base64Of(33)))
    }

    @Test
    fun decode_invalidBase64_vaultCorrupt() {
        val key = Base64.encode(decoded().exportPublicKey)

        assertCorrupt(mutated(key, "!!!not base64!!!"))
    }

    @Test
    fun decode_invalidTimestamp_vaultCorrupt() {
        assertCorrupt(mutated("2026-11-06T10:00:00Z", "yesterday"))
    }

    @Test
    fun decode_biometricFieldsOutOfStep_vaultCorrupt() {
        assertCorrupt(mutated("\"bio_key_alias\":null", "\"bio_key_alias\":\"patient_bio_v1\""))
        assertCorrupt(mutated("\"wrapped_mk_biometric\":null", "\"wrapped_mk_biometric\":\"AAAA\""))
    }

    @Test
    fun decode_counterOutOfRange_vaultCorrupt() {
        assertCorrupt(mutated("\"failed_attempts\":0", "\"failed_attempts\":-1"))
        assertCorrupt(mutated("\"lockout_until_elapsed\":0", "\"lockout_until_elapsed\":-1"))
        assertCorrupt(mutated("\"lockout_boot_count\":0", "\"lockout_boot_count\":-1"))
        assertCorrupt(mutated("\"wipe_after_failed_attempts\":0", "\"wipe_after_failed_attempts\":9"))
        assertCorrupt(mutated("\"wipe_after_failed_attempts\":0", "\"wipe_after_failed_attempts\":21"))
    }

    @Test
    fun decode_notJsonOrBadUtf8OrTooLarge_vaultCorrupt() {
        assertCorrupt("")
        assertCorrupt("not json")
        assertCorrupt("[]")
        assertEquals(corrupt, codec.decode(byteArrayOf(0xC3.toByte(), 0x28)))
        assertEquals(corrupt, codec.decode(ByteArray(MAX_ACCEPTED_BYTES + 1) { ' '.code.toByte() }))
    }

    @Test
    fun toString_always_isRedacted() {
        val result = codec.decode(golden.encodeToByteArray())

        assertEquals("VaultFile(format=1)", decoded().toString())
        assertEquals("Kdf(alg=argon2id)", decoded().kdf.toString())
        assertEquals("Ok(VaultFile(format=1))", result.toString())
    }

    private companion object {
        const val BIO_BLOB_BYTES = 120
        const val MAX_ACCEPTED_BYTES = 65_536
    }
}
