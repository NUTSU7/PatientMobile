package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BiometricKeyManager
import com.estcomputer.patient.domain.port.KeyManager
import kotlinx.coroutines.sync.withLock

internal class VaultFlows(
    val setup: SetupFlow,
    val unlock: PasswordUnlockFlow,
    val recovery: RecoveryFlow,
    val change: PasswordChangeFlow,
    val biometrics: BiometricFlow,
)

/** The one object behind [KeyManager] and [BiometricKeyManager]; every operation shares one mutex. */
internal class VaultKeyManager(
    private val flows: VaultFlows,
    private val inspector: StartupInspector,
    private val keyDerivation: KeyDerivation,
    private val keys: KeyDestroyer,
    private val context: VaultContext,
) : KeyManager, BiometricKeyManager by flows.biometrics {

    override suspend fun startupState(): StartupState = context.mutex.withLock { inspector.inspect() }

    override suspend fun prepareSetup(password: SecretChars): SetupResult = flows.setup.prepare(password)

    override suspend fun unlockWithPassword(password: SecretChars): UnlockResult = flows.unlock.unlock(password)

    override suspend fun unlockWithRecovery(words: SecretChars, newPassword: SecretChars): UnlockResult =
        flows.recovery.unlock(words, newPassword)

    override suspend fun changePassword(old: SecretChars?, new: SecretChars): ChangePasswordResult =
        flows.change.change(old, new)

    override suspend fun databaseKey(): SecretBytes =
        context.mutex.withLock { keyDerivation.deriveDbk(context.session.requireMk()) }

    override suspend fun fileKeyEncryptionKey(): SecretBytes =
        context.mutex.withLock { keyDerivation.deriveFkek(context.session.requireMk()) }

    override suspend fun lock() {
        flows.setup.abandonPending()
        context.mutex.withLock { context.session.clear() }
    }

    // ARH-09: the keys go first, so the vault is useless even if erasing the file fails.
    override suspend fun destroyKeys() {
        context.mutex.withLock {
            context.session.clear()
            keys.destroy()
        }
    }

    override suspend fun setWipeAfterFailedAttempts(attempts: Int?) {
        require(attempts == null || attempts in CryptoConstants.WIPE_AFTER_MIN..CryptoConstants.WIPE_AFTER_MAX) {
            "attempts out of range"
        }
        context.mutex.withLock {
            check(context.session.isUnlocked) { "vault is locked" }
            val vault = checkNotNull((context.store.read() as? VaultReadResult.Ok)?.vault) { "vault unreadable" }
            val updated = vault.copy(wipeAfterFailedAttempts = attempts ?: WIPE_DISABLED)
            check(context.store.write(updated) == VaultStoreResult.DONE) { "vault write failed" }
        }
    }

    private companion object {
        const val WIPE_DISABLED = 0
    }
}

/** Deletes both Keystore keys, then the vault file. */
internal class KeyDestroyer(
    private val hardware: HardwareWrap,
    private val biometricKeystore: BiometricKeystore,
    private val store: VaultStore,
) {
    suspend fun destroy() {
        hardware.drop(CryptoConstants.HW_KEY_ALIAS)
        biometricKeystore.deleteAlias(CryptoConstants.BIO_KEY_ALIAS)
        check(store.destroy() == VaultStoreResult.DONE) { "vault destroy failed" }
    }
}
