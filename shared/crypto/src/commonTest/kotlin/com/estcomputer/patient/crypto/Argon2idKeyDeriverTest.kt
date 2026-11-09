package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class Argon2idKeyDeriverTest {
    private class SingleDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val default: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    private class FakeArgon2id(private val timeSource: TestTimeSource) : Argon2id {
        var costPerIteration: Duration = 0.milliseconds
        var outOfMemoryAtKib: Set<Int> = emptySet()
        var onDerive: () -> Unit = {}
        val calls = mutableListOf<Pair<Int, Int>>()
        val keys = mutableListOf<SecretBytes>()

        override fun derive(password: SecretChars, params: KdfParams): KdfResult {
            calls.add(params.memoryKib to params.iterations)
            if (params.memoryKib in outOfMemoryAtKib) return KdfResult.Failed(KeyError.OUT_OF_MEMORY)
            timeSource += costPerIteration * params.iterations
            onDerive()
            return KdfResult.Derived(SecretBytes.wrap(ByteArray(KEY_SIZE)).also { keys.add(it) })
        }
    }

    private val timeSource = TestTimeSource()
    private val argon2 = FakeArgon2id(timeSource)
    private val password = SecretChars.copyOf("correct horse".toCharArray())
    private val salt = ByteArray(CryptoConstants.SALT_BYTES)

    private fun TestScope.deriver() =
        Argon2idKeyDeriver(argon2, SingleDispatcherProvider(StandardTestDispatcher(testScheduler)), timeSource)

    private fun params(
        salt: ByteArray = this.salt,
        memoryKib: Int = CryptoConstants.ARGON2_MEMORY_KIB,
        iterations: Int = CryptoConstants.ARGON2_ITERATIONS,
        parallelism: Int = CryptoConstants.ARGON2_PARALLELISM,
    ) = KdfParams(salt, memoryKib, iterations, parallelism)

    @Test
    fun derive_storedParams_usesThemWithoutCalibrating() = runTest {
        val result = deriver().derive(password, params(iterations = 7))

        assertIs<KdfResult.Derived>(result)
        assertEquals(listOf(CryptoConstants.ARGON2_MEMORY_KIB to 7), argon2.calls)
    }

    @Test
    fun derive_fallbackMemory_isAccepted() = runTest {
        val result = deriver().derive(password, params(memoryKib = CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB))

        assertIs<KdfResult.Derived>(result)
    }

    @Test
    fun derive_paramsOutOfBounds_vaultCorruptWithoutDeriving() = runTest {
        val tampered = listOf(
            params(memoryKib = CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB - 1),
            params(iterations = 0),
            params(iterations = CryptoConstants.MAX_ITERATIONS + 1),
            params(parallelism = 2),
            params(salt = ByteArray(CryptoConstants.SALT_BYTES - 1)),
        )

        for (stored in tampered) {
            assertEquals(KdfResult.Failed(KeyError.VAULT_CORRUPT), deriver().derive(password, stored))
        }
        assertTrue(argon2.calls.isEmpty())
    }

    @Test
    fun derive_outOfMemory_isReported() = runTest {
        argon2.outOfMemoryAtKib = setOf(CryptoConstants.ARGON2_MEMORY_KIB)

        assertEquals(KdfResult.Failed(KeyError.OUT_OF_MEMORY), deriver().derive(password, params()))
    }

    @Test
    fun derive_leavesPasswordToCaller() = runTest {
        deriver().derive(password, params())

        assertTrue(!password.isZeroed)
    }

    @Test
    fun derive_cancelledWhileDeriving_zeroesKey() = runTest {
        val deriver = deriver()
        lateinit var job: Job
        argon2.onDerive = { job.cancel() }

        job = launch { deriver.derive(password, params()) }
        job.join()

        assertTrue(job.isCancelled)
        assertTrue(argon2.keys.single().isZeroed)
    }

    @Test
    fun calibrate_slowDevice_keepsDefaultParams() = runTest {
        argon2.costPerIteration = 200.milliseconds

        val result = assertIs<CalibrationResult.Calibrated>(deriver().calibrate(password, salt))

        assertEquals(CryptoConstants.ARGON2_MEMORY_KIB, result.params.memoryKib)
        assertEquals(CryptoConstants.ARGON2_ITERATIONS, result.params.iterations)
        assertEquals(CryptoConstants.ARGON2_PARALLELISM, result.params.parallelism)
    }

    @Test
    fun calibrate_fastDevice_raisesIterationsUntilTarget() = runTest {
        argon2.costPerIteration = 100.milliseconds

        val result = assertIs<CalibrationResult.Calibrated>(deriver().calibrate(password, salt))

        assertEquals(5, result.params.iterations)
        assertEquals((3..5).map { CryptoConstants.ARGON2_MEMORY_KIB to it }, argon2.calls)
    }

    @Test
    fun calibrate_fastDevice_zeroesDiscardedKeysOnly() = runTest {
        argon2.costPerIteration = 100.milliseconds

        val result = assertIs<CalibrationResult.Calibrated>(deriver().calibrate(password, salt))

        assertTrue(argon2.keys.dropLast(1).all { it.isZeroed })
        assertTrue(!result.key.isZeroed)
        assertTrue(result.key === argon2.keys.last())
    }

    @Test
    fun calibrate_veryFastDevice_capsAtMaxIterations() = runTest {
        argon2.costPerIteration = 1.milliseconds

        val result = assertIs<CalibrationResult.Calibrated>(deriver().calibrate(password, salt))

        assertEquals(CryptoConstants.MAX_ITERATIONS, result.params.iterations)
    }

    @Test
    fun calibrate_outOfMemory_fallsBackToLowerMemory() = runTest {
        argon2.costPerIteration = 200.milliseconds
        argon2.outOfMemoryAtKib = setOf(CryptoConstants.ARGON2_MEMORY_KIB)

        val result = assertIs<CalibrationResult.Calibrated>(deriver().calibrate(password, salt))

        assertEquals(CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB, result.params.memoryKib)
        assertEquals(CryptoConstants.ARGON2_FALLBACK_ITERATIONS, result.params.iterations)
    }

    @Test
    fun calibrate_outOfMemoryTwice_fails() = runTest {
        argon2.outOfMemoryAtKib = setOf(CryptoConstants.ARGON2_MEMORY_KIB, CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB)

        assertEquals(CalibrationResult.Failed(KeyError.OUT_OF_MEMORY), deriver().calibrate(password, salt))
    }

    @Test
    fun calibrate_result_usesGivenSaltAndIsAcceptedOnUnlock() = runTest {
        argon2.costPerIteration = 200.milliseconds
        val salt = ByteArray(CryptoConstants.SALT_BYTES) { it.toByte() }

        val result = assertIs<CalibrationResult.Calibrated>(deriver().calibrate(password, salt))

        assertTrue(result.params.salt.contentEquals(salt))
        assertTrue(result.params.isWithinBounds)
    }

    @Test
    fun results_toString_isRedacted() {
        val key = SecretBytes.wrap(ByteArray(KEY_SIZE))

        assertEquals("Derived(***)", KdfResult.Derived(key).toString())
        assertEquals("Calibrated(***)", CalibrationResult.Calibrated(params(), key).toString())
    }

    private companion object {
        const val KEY_SIZE = 32
    }
}
