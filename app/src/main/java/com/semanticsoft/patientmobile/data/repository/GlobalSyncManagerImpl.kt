package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class GlobalSyncManagerImpl @Inject constructor() : GlobalSyncManager {

    private val _syncEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val debounceMutex = Mutex()
    private val lastEmitMs = AtomicLong(0L)

    override val syncEvents: SharedFlow<Unit> = _syncEvents.asSharedFlow()

    override suspend fun triggerSync() {
        val now = System.currentTimeMillis()
        val last = lastEmitMs.get()
        if (now - last < DEBOUNCE_MS) return
        debounceMutex.withLock {
            val lockedNow = System.currentTimeMillis()
            val lockedLast = lastEmitMs.get()
            if (lockedNow - lockedLast < DEBOUNCE_MS) return@withLock
            lastEmitMs.set(lockedNow)
            _syncEvents.emit(Unit)
        }
    }

    companion object {
        private const val DEBOUNCE_MS = 500L
    }
}
