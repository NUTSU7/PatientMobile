package com.estcomputer.patient.runtime

import android.content.Context
import com.estcomputer.patient.crypto.VaultCallbacks
import com.estcomputer.patient.crypto.VaultEnvironment
import com.estcomputer.patient.crypto.VaultKeys
import com.estcomputer.patient.crypto.VaultKeysFactory
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.port.BootInfo
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.session.SessionCoordinator
import com.estcomputer.patient.domain.usecase.RunStartup
import com.estcomputer.patient.domain.usecase.WipeAllData
import com.estcomputer.patient.platform.autolock.AndroidAutoLockEvents
import com.estcomputer.patient.platform.autolock.AutoLockPolicy
import com.estcomputer.patient.platform.boot.AndroidBootInfo
import com.estcomputer.patient.platform.screen.ScreenSecurityPolicy
import com.estcomputer.patient.platform.storage.AndroidAppFileOps
import com.estcomputer.patient.platform.storage.AndroidReminderCanceller
import com.estcomputer.patient.platform.storage.AndroidWipeMarker
import com.estcomputer.patient.platform.storage.AppDataInspector
import com.estcomputer.patient.platform.storage.AppFileOps
import com.estcomputer.patient.platform.storage.AppStorageWiper
import com.estcomputer.patient.platform.storage.ShareCacheCleaner
import com.estcomputer.patient.platform.storage.TransientFileCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Bindings that need an Android `Context`. Common bindings will live in `commonMain` and join this list.
 * The key manager is built lazily on first use: its word lists are read on the IO dispatcher.
 */
public val androidModules: List<Module> = listOf(
    module {
        single<DispatcherProvider> { SystemDispatchers() }
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + get<DispatcherProvider>().default) }
        single<BootInfo> { AndroidBootInfo(get<Context>().contentResolver) }
        single<AppFileOps> {
            val context = get<Context>()
            AndroidAppFileOps(context.noBackupFilesDir, context.cacheDir, get())
        }
        single { AppDataInspector(get()) }
        single<Deferred<VaultKeys>> { lazyKeys(get<Context>(), get(), get(), get(), get(), getWipe = { get() }) }
        single<KeyManager> { DeferredKeyManager { get<Deferred<VaultKeys>>().await().keyManager } }
        single {
            SessionCoordinator(get(), get(), listOf(ShareCacheCleaner(get())))
        }
        single {
            val context = get<Context>()
            WipeAllData(
                sessionCoordinator = get(),
                keyManager = get(),
                marker = AndroidWipeMarker(context.noBackupFilesDir, get()),
                storage = AppStorageWiper(get()),
                reminders = AndroidReminderCanceller(context),
            )
        }
        single { RunStartup(get(), get(), TransientFileCleaner(get()), get()) }
        single { AutoLockPolicy(get(), get(), get(), get()) }
        single { ScreenSecurityPolicy() }
        single { PatientRuntime(get(), get(), get(), AndroidAutoLockEvents(get(), get()), get()) }
    },
)

private fun lazyKeys(
    context: Context,
    scope: CoroutineScope,
    dispatchers: DispatcherProvider,
    bootInfo: BootInfo,
    inspector: AppDataInspector,
    getWipe: () -> WipeAllData,
): Deferred<VaultKeys> {
    val environment = VaultEnvironment(
        context = context,
        vaultDirectory = context.noBackupFilesDir,
        dispatchers = dispatchers,
        bootInfo = bootInfo,
    )
    val callbacks = VaultCallbacks(
        hasWipeMarker = inspector::hasWipeMarker,
        hasAppData = inspector::hasAppData,
        // Resolved at call time: the wipe is built against the key manager, which awaits the keys made here.
        requestWipe = { getWipe()() },
    )
    return scope.async(start = CoroutineStart.LAZY) { VaultKeysFactory.create(environment, callbacks) }
}
