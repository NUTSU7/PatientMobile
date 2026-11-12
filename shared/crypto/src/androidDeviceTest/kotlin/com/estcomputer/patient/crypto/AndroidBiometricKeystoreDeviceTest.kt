package com.estcomputer.patient.crypto

import androidx.test.platform.app.InstrumentationRegistry
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.SecretBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNull

// Needs a secure lock screen and an enrolled strong biometric; otherwise the tests are skipped.
// A real fingerprint cannot be automated, so the authenticated path is a manual check (SECURITY_NOTES.md).
class AndroidBiometricKeystoreDeviceTest {
    private class SingleDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val default: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    private val alias = "$ALIAS_PREFIX${System.nanoTime()}"
    private lateinit var keystore: AndroidBiometricKeystore

    private fun withKeystore(block: suspend () -> Unit) =
        runBlocking {
            val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            keystore = AndroidBiometricKeystore(context, SingleDispatcherProvider(dispatcher))
            block()
        }

    @After
    fun tearDown() = withKeystore { keystore.deleteAlias(alias) }

    private suspend fun generateOrSkip() {
        val result = keystore.generateBioKey(alias)
        assumeTrue(result is BiometricKeystoreGenerateResult.Ok)
    }

    @Test
    fun generateBioKey_whenEnrolled_reportsLevel() = withKeystore {
        generateOrSkip()

        assertIs<BiometricKeystoreBeginResult.Ready>(keystore.beginSeal(alias))
    }

    @Test
    fun doFinal_withoutAuthentication_throws() = withKeystore {
        generateOrSkip()
        val ready = assertIs<BiometricKeystoreBeginResult.Ready>(keystore.beginSeal(alias))
        val challenge = assertIs<KeystoreBiometricChallenge>(ready.challenge)

        assertFails { challenge.cipher.doFinal(ByteArray(CryptoConstants.KEY_SIZE_BYTES)) }
    }

    @Test
    fun seal_withoutAuthentication_doesNotReturnABlob() = withKeystore {
        generateOrSkip()
        val ready = assertIs<BiometricKeystoreBeginResult.Ready>(keystore.beginSeal(alias))

        val outcome = runCatching {
            keystore.seal(ready.challenge, SecretBytes.copyOf(ByteArray(CryptoConstants.KEY_SIZE_BYTES)), AAD)
        }

        assertEquals(false, outcome.getOrNull() is BiometricKeystoreSealResult.Ok)
    }

    @Test
    fun beginOpen_withShortBlob_isRejected() = withKeystore {
        generateOrSkip()

        assertEquals(
            BiometricKeystoreBeginResult.Failed(BiometricKeystoreError.BLOB_REJECTED),
            keystore.beginOpen(alias, ByteArray(1)),
        )
    }

    @Test
    fun beginOpen_withNonceSizedBlob_succeeds() = withKeystore {
        generateOrSkip()

        assertIs<BiometricKeystoreBeginResult.Ready>(keystore.beginOpen(alias, ByteArray(MIN_BLOB_BYTES)))
    }

    @Test
    fun begin_afterDelete_reportsMissingKey() = withKeystore {
        generateOrSkip()

        keystore.deleteAlias(alias)

        assertEquals(
            BiometricKeystoreBeginResult.Failed(BiometricKeystoreError.KEY_MISSING),
            keystore.beginSeal(alias),
        )
        assertEquals(
            BiometricKeystoreBeginResult.Failed(BiometricKeystoreError.KEY_MISSING),
            keystore.beginOpen(alias, ByteArray(MIN_BLOB_BYTES)),
        )
    }

    @Test
    fun deleteAlias_whenAbsent_doesNothing() = withKeystore {
        keystore.deleteAlias(alias)
        keystore.deleteAlias(alias)
    }

    private companion object {
        const val ALIAS_PREFIX = "patient_test_bio_"
        const val AAD = CryptoConstants.AAD_MK_BIO
        const val MIN_BLOB_BYTES = CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.GCM_TAG_BYTES
    }
}
