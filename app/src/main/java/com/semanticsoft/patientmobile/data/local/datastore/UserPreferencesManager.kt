package com.semanticsoft.patientmobile.data.local.datastore

import android.content.Context

class UserPreferencesManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setDarkThemeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    fun isDarkThemeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_THEME, false)

    fun setLastSyncEpochMillis(value: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_EPOCH_MILLIS, value).apply()
    }

    fun getLastSyncEpochMillis(): Long = prefs.getLong(KEY_LAST_SYNC_EPOCH_MILLIS, 0L)

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_DARK_THEME = "dark_theme_enabled"
        private const val KEY_LAST_SYNC_EPOCH_MILLIS = "last_sync_epoch_millis"
    }
}
