package com.semanticsoft.patientmobile.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesManager(
    private val dataStore: DataStore<Preferences>
) {

    fun darkThemeEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_DARK_THEME] ?: false }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = enabled
        }
    }

    fun lastSyncEpochMillis(): Flow<Long> =
        dataStore.data.map { prefs -> prefs[KEY_LAST_SYNC_EPOCH_MILLIS] ?: 0L }

    suspend fun setLastSyncEpochMillis(value: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_SYNC_EPOCH_MILLIS] = value
        }
    }

    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
        private val KEY_LAST_SYNC_EPOCH_MILLIS = longPreferencesKey("last_sync_epoch_millis")
    }
}
