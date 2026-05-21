package com.semanticsoft.patientmobile.data.local.datastore

import android.content.Context
import com.semanticsoft.patientmobile.domain.model.User

class EncryptedTokenManager(context: Context) : TokenManager {
    private val secureTokenStore = SecureTokenStore(context.applicationContext)

    override fun saveTokens(access: String, refresh: String) {
        secureTokenStore.saveTokens(access, refresh)
    }

    override fun getAccessToken(): String? = secureTokenStore.getAccessToken()

    override fun getRefreshToken(): String? = secureTokenStore.getRefreshToken()

    override fun clearTokens() {
        secureTokenStore.clearTokens()
    }

    override fun saveUser(user: User) {
        secureTokenStore.saveUser(user)
    }

    override fun getUser(): User? = secureTokenStore.getUser()

    override fun clearUser() {
        secureTokenStore.clearUser()
    }
}
