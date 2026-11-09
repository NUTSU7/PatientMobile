package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

internal class Argon2idKeyDeriver(
    private val argon2: Argon2id,
    private val dispatchers: DispatcherProvider,
    private val timeSource: TimeSource,
) : PasswordKdf, KdfCalibrator {

    override suspend fun derive(password: SecretChars, params: KdfParams): KdfResult {
        if (!params.isWithinBounds) return KdfResult.Failed(KeyError.VAULT_CORRUPT)
        return deriveOnDefault(password, params)
    }

    override suspend fun calibrate(password: SecretChars, salt: ByteArray): CalibrationResult {
        for ((memoryKib, startIterations) in PROFILES) {
            val result = calibrateProfile(password, salt, memoryKib, startIterations)
            if (result !is CalibrationResult.Failed || result.error != KeyError.OUT_OF_MEMORY) return result
        }
        return CalibrationResult.Failed(KeyError.OUT_OF_MEMORY)
    }

    private suspend fun calibrateProfile(
        password: SecretChars,
        salt: ByteArray,
        memoryKib: Int,
        startIterations: Int,
    ): CalibrationResult {
        var iterations = startIterations
        while (true) {
            val params = KdfParams(salt.copyOf(), memoryKib, iterations, CryptoConstants.ARGON2_PARALLELISM)
            val mark = timeSource.markNow()
            val key = when (val result = deriveOnDefault(password, params)) {
                is KdfResult.Derived -> result.key
                is KdfResult.Failed -> return CalibrationResult.Failed(result.error)
            }
            if (mark.elapsedNow() >= TARGET || iterations == CryptoConstants.MAX_ITERATIONS) {
                return CalibrationResult.Calibrated(params, key)
            }
            key.zero()
            iterations++
        }
    }

    private suspend fun deriveOnDefault(password: SecretChars, params: KdfParams): KdfResult {
        var result: KdfResult? = null
        try {
            withContext(dispatchers.default) { result = argon2.derive(password, params) }
            return requireNotNull(result)
        } catch (e: CancellationException) {
            // Argon2 can't be interrupted, so a key that finishes after cancellation is discarded.
            (result as? KdfResult.Derived)?.key?.zero()
            throw e
        }
    }

    private companion object {
        val TARGET = CryptoConstants.CALIBRATION_TARGET_MS.milliseconds

        // D7: on OutOfMemoryError fall back to half the memory with more iterations.
        val PROFILES = listOf(
            CryptoConstants.ARGON2_MEMORY_KIB to CryptoConstants.ARGON2_ITERATIONS,
            CryptoConstants.ARGON2_FALLBACK_MEMORY_KIB to CryptoConstants.ARGON2_FALLBACK_ITERATIONS,
        )
    }
}
