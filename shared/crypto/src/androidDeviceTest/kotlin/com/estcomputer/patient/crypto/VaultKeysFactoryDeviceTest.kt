package com.estcomputer.patient.crypto

import android.app.KeyguardManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BootInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/** The whole key hierarchy on a real Keystore: setup, lock, unlock and restart. Needs a secure lock screen. */
class VaultKeysFactoryDeviceTest {
    private class SingleDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val default: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    private class FixedBootInfo : BootInfo {
        override fun bootCount(): Int = BOOT_COUNT
        override fun elapsedRealtimeMillis(): Long = System.nanoTime() / NANOS_PER_MILLI
    }

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val vaultDirectory = File(context.cacheDir, "vault-keys-test")
    private val wipeRequests = mutableListOf<String>()

    @Before
    fun setUp() {
        assumeTrue(
            "needs a secure lock screen",
            context.getSystemService(KeyguardManager::class.java).isDeviceSecure,
        )
        vaultDirectory.deleteRecursively()
        vaultDirectory.mkdirs()
    }

    @After
    fun tearDown() {
        // Each run creates the fixed-alias keys, so they are removed even when an assertion failed.
        runBlocking { newKeys().keyManager.destroyKeys() }
        vaultDirectory.deleteRecursively()
    }

    private suspend fun newKeys(): VaultKeys {
        val dispatcher = currentCoroutineContext()[ContinuationInterceptor] as CoroutineDispatcher
        val environment = VaultEnvironment(
            context = context,
            vaultDirectory = vaultDirectory,
            dispatchers = SingleDispatcherProvider(dispatcher),
            bootInfo = FixedBootInfo(),
        )
        val callbacks = VaultCallbacks(
            hasWipeMarker = { false },
            hasAppData = { false },
            requestWipe = { wipeRequests += "wipe" },
        )
        return VaultKeysFactory.create(environment, callbacks)
    }

    private fun password(text: String): SecretChars = SecretChars.copyOf(text.toCharArray())

    @Test
    fun setupLockUnlock_throughTheFactory_roundTrips() = runBlocking<Unit> {
        val keys = newKeys().keyManager
        assertEquals(StartupState.Clean, keys.startupState())

        val ready = assertIs<SetupResult.Ready>(keys.prepareSetup(password(PASSWORD)))
        ready.pending.commit()
        ready.pending.recoveryWords.zero()
        keys.databaseKey().use { assertEquals(CryptoConstants.KEY_SIZE_BYTES, it.copyOf().size) }

        keys.lock()
        assertFailsWith<IllegalStateException> { keys.databaseKey() }

        val wrong = keys.unlockWithPassword(password("not the password 1"))
        assertEquals(UnlockResult.Failed(KeyError.WRONG_PASSWORD), wrong)
        assertIs<UnlockResult.Unlocked>(keys.unlockWithPassword(password(PASSWORD)))
        keys.databaseKey().use { assertEquals(CryptoConstants.KEY_SIZE_BYTES, it.copyOf().size) }
        assertEquals(emptyList(), wipeRequests)
    }

    @Test
    fun unlock_afterRestart_opensTheVaultWrittenByAnotherInstance() = runBlocking<Unit> {
        val first = newKeys().keyManager
        val ready = assertIs<SetupResult.Ready>(first.prepareSetup(password(PASSWORD)))
        ready.pending.commit()
        ready.pending.recoveryWords.zero()
        first.lock()

        val second = newKeys().keyManager

        assertEquals(StartupState.VaultPresent, second.startupState())
        assertIs<UnlockResult.Unlocked>(second.unlockWithPassword(password(PASSWORD)))
    }

    private companion object {
        const val PASSWORD = "correct horse battery staple 42"
        const val BOOT_COUNT = 7
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
