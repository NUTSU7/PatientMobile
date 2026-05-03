package com.semanticsoft.patientmobile.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class UserPreferencesManager(
    private val dataStore: DataStore<Preferences>
) {

    fun setDarkThemeEnabled(enabled: Boolean) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs[KEY_DARK_THEME] = enabled
            }
        }
    }

    fun isDarkThemeEnabled(): Boolean = runBlocking {
        dataStore.data.map { prefs -> prefs[KEY_DARK_THEME] ?: false }.first()
    }

    fun setLastSyncEpochMillis(value: Long) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs[KEY_LAST_SYNC_EPOCH_MILLIS] = value
            }
        }
    }

    fun getLastSyncEpochMillis(): Long = runBlocking {
        dataStore.data.map { prefs -> prefs[KEY_LAST_SYNC_EPOCH_MILLIS] ?: 0L }.first()
    }

    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
        private val KEY_LAST_SYNC_EPOCH_MILLIS = longPreferencesKey("last_sync_epoch_millis")
    }
}
