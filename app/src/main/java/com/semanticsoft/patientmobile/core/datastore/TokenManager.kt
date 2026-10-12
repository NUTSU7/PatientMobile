package com.semanticsoft.patientmobile.core.datastore

import com.semanticsoft.patientmobile.domain.model.common.User

interface TokenManager {
    fun saveTokens(access: String, refresh: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
    fun saveUser(user: User)
    fun getUser(): User?
    fun clearUser()
}
