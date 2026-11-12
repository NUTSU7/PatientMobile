package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.PendingSetup
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupCommitResult
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import kotlinx.coroutines.sync.withLock

/** Two-phase first-time setup: everything is built in memory, and nothing is on disk until commit. */
internal class SetupFlow(
    private val inspector: StartupInspector,
    private val validator: PasswordPolicyValidator,
    private val calibrator: KdfCalibrator,
    private val hardware: HardwareWrap,
    private val tools: KeyToolbox,
    private val context: VaultContext,
) {
    private var pending: PendingSetup? = null

    /** Zeroes a setup that was prepared but not committed, e.g. on lock. */
    suspend fun abandonPending() {
        context.mutex.withLock {
            pending?.abandon()
            pending = null
        }
    }

    /** Valid only in [StartupState.Clean]; anything else is a caller bug. */
    suspend fun prepare(password: SecretChars): SetupResult =
        context.mutex.withLock {
            check(inspector.inspect() == StartupState.Clean) { "setup needs a clean install" }
            pending?.abandon()
            pending = null
            if (context.deviceSecurity.isDeviceSecure()) {
                when (val checked = validator.validate(password)) {
                    is PasswordCheck.Rejected -> SetupResult.PasswordRejected(checked.reason)
                    is PasswordCheck.Ok -> buildAndClear(checked.normalized)
                }
            } else {
                SetupResult.Failed(KeyError.DEVICE_NOT_SECURE)
            }
        }

    private suspend fun buildAndClear(normalized: SecretChars): SetupResult =
        try {
            build(normalized)
        } finally {
            normalized.zero()
        }

    private suspend fun build(normalized: SecretChars): SetupResult {
        val mk = SecretBytes.wrap(tools.random.nextBytes(CryptoConstants.KEY_SIZE_BYTES))
        val recoverySecret = SecretBytes.wrap(tools.random.nextBytes(CryptoConstants.KEY_SIZE_BYTES))
        var isPrepared = false
        try {
            val result = calibrateAndWrap(normalized, mk, recoverySecret)
            isPrepared = result is SetupResult.Ready
            return result
        } finally {
            recoverySecret.zero()
            if (!isPrepared) mk.zero()
        }
    }

    private suspend fun calibrateAndWrap(
        normalized: SecretChars,
        mk: SecretBytes,
        recoverySecret: SecretBytes,
    ): SetupResult =
        when (val calibrated = calibrator.calibrate(normalized, tools.random.nextBytes(CryptoConstants.SALT_BYTES))) {
            is CalibrationResult.Failed -> SetupResult.Failed(calibrated.error)
            is CalibrationResult.Calibrated ->
                try {
                    SetupResult.Ready(wrap(calibrated, mk, recoverySecret))
                } finally {
                    calibrated.key.zero()
                }
        }

    private fun wrap(
        calibrated: CalibrationResult.Calibrated,
        mk: SecretBytes,
        recoverySecret: SecretBytes,
    ): PendingSetup {
        val inner = tools.aead.seal(calibrated.key, mk, CryptoConstants.AAD_MK_PASSWORD)
        val rwk = tools.keyDerivation.deriveRwk(recoverySecret)
        val wrappedRecovery = try {
            tools.aead.seal(rwk, mk, CryptoConstants.AAD_MK_RECOVERY)
        } finally {
            rwk.zero()
        }
        val prepared = Prepared(
            mk = mk,
            inner = inner,
            kdf = calibrated.params,
            wrappedRecovery = wrappedRecovery,
            exportPublicKey = tools.exportKeyDeriver.publicKey(recoverySecret),
        )
        return PendingSetupImpl(prepared, tools.bip39.encode(recoverySecret)).also { pending = it }
    }

    private class Prepared(
        val mk: SecretBytes,
        val inner: ByteArray,
        val kdf: KdfParams,
        val wrappedRecovery: ByteArray,
        val exportPublicKey: ByteArray,
    )

    private inner class PendingSetupImpl(
        private val prepared: Prepared,
        override val recoveryWords: SecretChars,
    ) : PendingSetup {
        private var isDone = false

        override suspend fun commit(): SetupCommitResult =
            context.mutex.withLock {
                check(!isDone) { "setup already finished" }
                val result = write(prepared)
                // On failure the patient may retry commit, so only a success consumes the setup.
                if (result is SetupCommitResult.Committed) release(keepMk = true)
                result
            }

        override fun abandon() {
            if (!isDone) release(keepMk = false)
        }

        private fun release(keepMk: Boolean) {
            isDone = true
            recoveryWords.zero()
            prepared.inner.fill(0)
            if (!keepMk) prepared.mk.zero()
        }
    }

    private suspend fun write(prepared: Prepared): SetupCommitResult =
        when (hardware.create(CryptoConstants.HW_KEY_ALIAS)) {
            KeystoreGenerateResult.DeviceNotSecure -> SetupCommitResult.Failed(KeyError.DEVICE_NOT_SECURE)
            is KeystoreGenerateResult.Ok -> sealAndStore(prepared)
        }

    private suspend fun sealAndStore(prepared: Prepared): SetupCommitResult =
        when (val wrapped = hardware.wrap(CryptoConstants.HW_KEY_ALIAS, prepared.inner)) {
            is HardwareWrapResult.Failed -> failAndDropKey(wrapped.error)
            is HardwareWrapResult.Ok -> storeVault(prepared, wrapped.blob)
        }

    private suspend fun storeVault(prepared: Prepared, wrappedPassword: ByteArray): SetupCommitResult {
        val now = context.clock.now()
        val vault = VaultFile(
            format = CryptoConstants.VAULT_FORMAT,
            createdAt = now.toString(),
            kdf = VaultFile.Kdf(
                alg = ARGON2ID,
                salt = prepared.kdf.salt,
                memoryKib = prepared.kdf.memoryKib,
                iterations = prepared.kdf.iterations,
                parallelism = prepared.kdf.parallelism,
            ),
            hwKeyAlias = CryptoConstants.HW_KEY_ALIAS,
            wrappedMkPassword = wrappedPassword,
            wrappedMkRecovery = prepared.wrappedRecovery,
            wrappedMkBiometric = null,
            bioKeyAlias = null,
            exportPublicKey = prepared.exportPublicKey,
            failedAttempts = 0,
            wipeAfterFailedAttempts = WIPE_DISABLED,
            lockoutUntilElapsed = 0,
            lockoutBootCount = context.bootInfo.bootCount(),
        )
        return if (context.store.write(vault) == VaultStoreResult.DONE) {
            context.session.start(prepared.mk, now, context.bootInfo.elapsedRealtimeMillis())
            SetupCommitResult.Committed
        } else {
            failAndDropKey(KeyError.STORAGE_FAILED)
        }
    }

    private suspend fun failAndDropKey(error: KeyError): SetupCommitResult {
        hardware.drop(CryptoConstants.HW_KEY_ALIAS)
        return SetupCommitResult.Failed(error)
    }

    private companion object {
        const val ARGON2ID = "argon2id"
        const val WIPE_DISABLED = 0
    }
}
