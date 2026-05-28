package com.semanticsoft.patientmobile.domain.repository

import kotlinx.coroutines.flow.SharedFlow

interface GlobalSyncManager {
    val syncEvents: SharedFlow<Unit>
    suspend fun triggerSync()
}
