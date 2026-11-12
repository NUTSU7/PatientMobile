package com.estcomputer.patient.runtime

import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.model.WipeSettingResult
import com.estcomputer.patient.domain.port.KeyManager

/**
 * Forwards to a key manager that is built later. The wipe needs the key manager and the key manager needs the
 * wipe, so the wipe is built against this and the real one is awaited on first use.
 */
internal class DeferredKeyManager(private val target: suspend () -> KeyManager) : KeyManager {
    override suspend fun startupState(): StartupState = target().startupState()

    override suspend fun prepareSetup(password: SecretChars): SetupResult = target().prepareSetup(password)

    override suspend fun unlockWithPassword(password: SecretChars): UnlockResult =
        target().unlockWithPassword(password)

    override suspend fun unlockWithRecovery(words: SecretChars, newPassword: SecretChars): UnlockResult =
        target().unlockWithRecovery(words, newPassword)

    override suspend fun changePassword(old: SecretChars?, new: SecretChars): ChangePasswordResult =
        target().changePassword(old, new)

    override suspend fun databaseKey(): SecretBytes = target().databaseKey()

    override suspend fun fileKeyEncryptionKey(): SecretBytes = target().fileKeyEncryptionKey()

    override suspend fun lock() = target().lock()

    override suspend fun destroyKeys(): KeyDestructionResult = target().destroyKeys()

    override suspend fun setWipeAfterFailedAttempts(attempts: Int?): WipeSettingResult =
        target().setWipeAfterFailedAttempts(attempts)
}
