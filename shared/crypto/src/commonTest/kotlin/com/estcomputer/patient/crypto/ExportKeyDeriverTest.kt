package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportKeyDeriverTest {
    private class FixedHkdf(private val output: ByteArray) : Hkdf {
        val infos = mutableListOf<String>()
        val results = mutableListOf<SecretBytes>()

        override fun derive(ikm: SecretBytes, info: String): SecretBytes {
            infos.add(info)
            return SecretBytes.copyOf(output).also { results.add(it) }
        }
    }

    private class RecordingX25519(private val publicKey: ByteArray) : X25519 {
        var privateSeen: ByteArray? = null

        override fun publicKey(privateKey: SecretBytes): ByteArray {
            privateSeen = privateKey.read { it.copyOf() }
            return publicKey
        }
    }

    private val privateKey = ByteArray(CryptoConstants.KEY_SIZE_BYTES) { it.toByte() }
    private val publicKey = ByteArray(CryptoConstants.KEY_SIZE_BYTES) { (it + 1).toByte() }
    private val hkdf = FixedHkdf(privateKey)
    private val x25519 = RecordingX25519(publicKey)
    private val deriver = ExportKeyDeriver(KeyDerivation(hkdf), x25519)
    private val recoverySecret = SecretBytes.copyOf(ByteArray(CryptoConstants.KEY_SIZE_BYTES))

    @Test
    fun publicKey_usesExportKemInfoAndReturnsKey() {
        val result = deriver.publicKey(recoverySecret)

        assertEquals(listOf(CryptoConstants.HKDF_INFO_EXPORT_KEM), hkdf.infos)
        assertContentEquals(privateKey, x25519.privateSeen)
        assertContentEquals(publicKey, result)
    }

    @Test
    fun publicKey_afterCall_zeroesPrivateKey() {
        deriver.publicKey(recoverySecret)

        assertTrue(hkdf.results.single().isZeroed)
    }

    @Test
    fun publicKey_leavesRecoverySecretToCaller() {
        deriver.publicKey(recoverySecret)

        assertTrue(!recoverySecret.isZeroed)
    }
}
