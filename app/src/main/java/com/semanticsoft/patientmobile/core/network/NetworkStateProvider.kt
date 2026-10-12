package com.semanticsoft.patientmobile.core.network

fun interface NetworkStateProvider {
    fun isOnline(): Boolean
}

object AlwaysOnlineStateProvider : NetworkStateProvider {
    override fun isOnline(): Boolean = true
}
