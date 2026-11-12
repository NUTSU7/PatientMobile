package com.estcomputer.patient.crypto

import androidx.test.platform.app.InstrumentationRegistry
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AndroidKeystoreCipherDeviceTest {
    private class SingleDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val default: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    private val alias = "$ALIAS_PREFIX${System.nanoTime()}"
    private val aad = CryptoConstants.AAD_MK_HW
    private val message = "inner wrapped mk".encodeToByteArray()
    private lateinit var cipher: AndroidKeystoreCipher

    // The runBlocking loop doubles as the injected dispatcher, so the tests need no Dispatchers.* reference.
    private fun withCipher(block: suspend () -> Unit) =
        runBlocking {
            val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            cipher = AndroidKeystoreCipher(context, SingleDispatcherProvider(dispatcher))
            block()
        }

    @After
    fun tearDown() = withCipher { cipher.deleteAlias(alias) }

    private suspend fun seal(): ByteArray {
        val result = cipher.seal(alias, SecretBytes.copyOf(message), aad)
        return assertIs<KeystoreSealResult.Ok>(result).blob
    }

    @Test
    fun sealOpen_roundTrip_returnsPlaintext() = withCipher {
        cipher.generateHwKey(alias)
        val blob = seal()

        val opened = assertIs<KeystoreOpenResult.Ok>(cipher.open(alias, blob, aad))

        assertContentEquals(message, opened.plaintext.use { it.copyOf() })
        assertEquals(CryptoConstants.GCM_NONCE_BYTES + message.size + CryptoConstants.GCM_TAG_BYTES, blob.size)
    }

    @Test
    fun open_withWrongAad_requiresRecovery() = withCipher {
        cipher.generateHwKey(alias)
        val blob = seal()

        val result = cipher.open(alias, blob, CryptoConstants.AAD_MK_PASSWORD)

        assertEquals(KeystoreOpenResult.Failed(KeystoreError.RECOVERY_REQUIRED), result)
    }

    @Test
    fun open_withTamperedBlob_requiresRecovery() = withCipher {
        cipher.generateHwKey(alias)
        val blob = seal()
        blob[blob.lastIndex] = (blob.last().toInt() xor 1).toByte()

        assertEquals(
            KeystoreOpenResult.Failed(KeystoreError.RECOVERY_REQUIRED),
            cipher.open(alias, blob, aad),
        )
        assertEquals(
            KeystoreOpenResult.Failed(KeystoreError.RECOVERY_REQUIRED),
            cipher.open(alias, ByteArray(1), aad),
        )
    }

    @Test
    fun sealAndOpen_afterDelete_reportMissingKey() = withCipher {
        cipher.generateHwKey(alias)
        val blob = seal()

        cipher.deleteAlias(alias)

        assertEquals(KeystoreOpenResult.Failed(KeystoreError.HW_KEY_MISSING), cipher.open(alias, blob, aad))
        assertEquals(
            KeystoreSealResult.Failed(KeystoreError.HW_KEY_MISSING),
            cipher.seal(alias, SecretBytes.copyOf(message), aad),
        )
        assertNull(cipher.securityLevel(alias))
    }

    @Test
    fun seal_twice_usesDifferentNonces() = withCipher {
        cipher.generateHwKey(alias)

        val first = seal().copyOfRange(0, CryptoConstants.GCM_NONCE_BYTES)
        val second = seal().copyOfRange(0, CryptoConstants.GCM_NONCE_BYTES)

        assertNotEquals(first.toList(), second.toList())
    }

    @Test
    fun deleteAlias_whenAbsent_doesNothing() = withCipher {
        cipher.deleteAlias(alias)
        cipher.deleteAlias(alias)
    }

    @Test
    fun generateHwKey_reportsLevelOfStoredKey() = withCipher {
        val level = cipher.generateHwKey(alias)

        assertNotNull(cipher.securityLevel(alias))
        assertEquals(level, cipher.securityLevel(alias))
    }

    private companion object {
        const val ALIAS_PREFIX = "patient_test_hw_"
    }
}
