package com.semanticsoft.patientmobile.data.repository

fun interface NetworkStateProvider {
    fun isOnline(): Boolean
}

object AlwaysOnlineStateProvider : NetworkStateProvider {
    override fun isOnline(): Boolean = true
}
