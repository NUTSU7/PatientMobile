package com.estcomputer.patient.domain.session

import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.model.WipeSettingResult
import com.estcomputer.patient.domain.port.KeyManager

/** Records every call that matters to ordering. Everything else is outside the tests that use it. */
class FakeKeyManager(private val events: MutableList<String>) : KeyManager {
    var destruction: KeyDestructionResult = KeyDestructionResult.DONE
    var lockCalls: Int = 0
        private set

    override suspend fun lock() {
        lockCalls++
        events += "keys.lock"
    }

    override suspend fun destroyKeys(): KeyDestructionResult {
        events += "keys.destroy"
        return destruction
    }

    override suspend fun startupState(): StartupState = unused()
    override suspend fun prepareSetup(password: SecretChars): SetupResult = unused()
    override suspend fun unlockWithPassword(password: SecretChars): UnlockResult = unused()
    override suspend fun unlockWithRecovery(words: SecretChars, newPassword: SecretChars): UnlockResult = unused()
    override suspend fun changePassword(old: SecretChars?, new: SecretChars): ChangePasswordResult = unused()
    override suspend fun databaseKey(): SecretBytes = unused()
    override suspend fun fileKeyEncryptionKey(): SecretBytes = unused()
    override suspend fun setWipeAfterFailedAttempts(attempts: Int?, password: SecretChars?): WipeSettingResult =
        unused()

    private fun unused(): Nothing = error("not used by these tests")
}
