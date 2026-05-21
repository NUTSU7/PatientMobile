package com.semanticsoft.patientmobile.data.local.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.semanticsoft.patientmobile.domain.model.User
import kotlinx.serialization.json.Json

class SecureTokenStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).apply()
    }

    fun saveUser(user: User) {
        prefs.edit()
            .putString(KEY_USER_JSON, json.encodeToString(User.serializer(), user))
            .apply()
    }

    fun getUser(): User? {
        return prefs.getString(KEY_USER_JSON, null)?.let {
            runCatching { json.decodeFromString(User.serializer(), it) }.getOrNull()
        }
    }

    fun clearUser() {
        prefs.edit().remove(KEY_USER_JSON).apply()
    }

    companion object {
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_JSON = "user_json"
    }
}
