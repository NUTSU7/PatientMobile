package com.estcomputer.patient.platform.autolock

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.model.WipeSettingResult
import com.estcomputer.patient.domain.port.BootInfo
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.session.SessionCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import kotlin.test.assertNotEquals

private class NoKeys : KeyManager {
    override suspend fun lock() = Unit
    override suspend fun destroyKeys(): KeyDestructionResult = KeyDestructionResult.DONE
    override suspend fun startupState(): StartupState = error("unused")
    override suspend fun prepareSetup(password: SecretChars): SetupResult = error("unused")
    override suspend fun unlockWithPassword(password: SecretChars): UnlockResult = error("unused")
    override suspend fun unlockWithRecovery(words: SecretChars, newPassword: SecretChars): UnlockResult =
        error("unused")
    override suspend fun changePassword(old: SecretChars?, new: SecretChars): ChangePasswordResult = error("unused")
    override suspend fun databaseKey(): SecretBytes = error("unused")
    override suspend fun fileKeyEncryptionKey(): SecretBytes = error("unused")
    override suspend fun setWipeAfterFailedAttempts(attempts: Int?): WipeSettingResult = error("unused")
}

class AndroidAutoLockEventsDeviceTest {

    private val dispatchers = object : DispatcherProvider {
        override val default = Dispatchers.Default
        override val io = Dispatchers.IO
        override val main = Dispatchers.Default
    }

    private val clock = object : BootInfo {
        override fun bootCount(): Int = 1
        override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
    }

    @Test
    fun screenOff_locksTheSession() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = SessionCoordinator(scope, NoKeys(), emptyList())
        val policy = AutoLockPolicy(coordinator, clock, dispatchers, scope)
        instrumentation.runOnMainSync {
            AndroidAutoLockEvents(instrumentation.targetContext, policy).register()
        }
        policy.start()
        coordinator.onUnlocked()

        try {
            instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_SLEEP").close()
            withTimeout(SCREEN_OFF_TIMEOUT_MILLIS) { coordinator.lockState.first { it != LockState.Unlocked } }
        } finally {
            instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_WAKEUP").close()
            scope.cancel()
        }

        assertNotEquals(LockState.Unlocked, coordinator.lockState.value)
    }

    private companion object {
        const val SCREEN_OFF_TIMEOUT_MILLIS = 10_000L
    }
}
