package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalSyncManagerImpl @Inject constructor() : GlobalSyncManager {

    private val _syncEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override val syncEvents: SharedFlow<Unit> = _syncEvents.asSharedFlow()

    override suspend fun triggerSync() {
        _syncEvents.emit(Unit)
    }
}
