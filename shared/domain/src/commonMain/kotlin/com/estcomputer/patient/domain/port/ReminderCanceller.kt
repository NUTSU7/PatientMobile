package com.estcomputer.patient.domain.port

public fun interface ReminderCanceller {
    /** Cancels every pending reminder and shown notification. Safe to call when there are none. */
    public suspend fun cancelAll()
}
