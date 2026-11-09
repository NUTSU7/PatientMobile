package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyDerivationTest {
    private class RecordingHkdf : Hkdf {
        val infos = mutableListOf<String>()

        override fun derive(ikm: SecretBytes, info: String): SecretBytes {
            infos.add(info)
            return SecretBytes.wrap(ByteArray(CryptoConstants.KEY_SIZE_BYTES))
        }
    }

    private val hkdf = RecordingHkdf()
    private val derivation = KeyDerivation(hkdf)
    private val key = SecretBytes.copyOf(ByteArray(CryptoConstants.KEY_SIZE_BYTES))

    @Test
    fun deriveDbk_usesDbInfo() {
        derivation.deriveDbk(key)

        assertEquals(listOf("patient/db/v1"), hkdf.infos)
    }

    @Test
    fun deriveFkek_usesFileKekInfo() {
        derivation.deriveFkek(key)

        assertEquals(listOf("patient/file-kek/v1"), hkdf.infos)
    }

    @Test
    fun deriveRwk_usesRecoveryWrapInfo() {
        derivation.deriveRwk(key)

        assertEquals(listOf("patient/recovery-wrap/v1"), hkdf.infos)
    }

    @Test
    fun deriveExportPrivate_usesExportKemInfo() {
        derivation.deriveExportPrivate(key)

        assertEquals(listOf("patient/export-kem/v1"), hkdf.infos)
    }
}
