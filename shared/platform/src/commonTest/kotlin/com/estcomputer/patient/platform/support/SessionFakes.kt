package com.estcomputer.patient.platform.support

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.model.WipeSettingResult
import com.estcomputer.patient.domain.port.BootInfo
import com.estcomputer.patient.domain.port.KeyManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

class FakeKeyManager : KeyManager {
    var lockCalls: Int = 0
        private set

    override suspend fun lock() {
        lockCalls++
    }

    override suspend fun destroyKeys(): KeyDestructionResult = KeyDestructionResult.DONE
    override suspend fun startupState(): StartupState = unused()
    override suspend fun prepareSetup(password: SecretChars): SetupResult = unused()
    override suspend fun unlockWithPassword(password: SecretChars): UnlockResult = unused()
    override suspend fun unlockWithRecovery(words: SecretChars, newPassword: SecretChars): UnlockResult = unused()
    override suspend fun changePassword(old: SecretChars?, new: SecretChars): ChangePasswordResult = unused()
    override suspend fun databaseKey(): SecretBytes = unused()
    override suspend fun fileKeyEncryptionKey(): SecretBytes = unused()
    override suspend fun setWipeAfterFailedAttempts(attempts: Int?): WipeSettingResult = unused()

    private fun unused(): Nothing = error("not used by these tests")
}

class TestDispatchers(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}

/** Reads the test scheduler's virtual time. [frozenMillis] adds time that passes without any timer firing. */
class SchedulerBootInfo(private val scheduler: TestCoroutineScheduler) : BootInfo {
    var frozenMillis: Long = 0

    override fun bootCount(): Int = 1
    override fun elapsedRealtimeMillis(): Long = scheduler.currentTime + frozenMillis
}
