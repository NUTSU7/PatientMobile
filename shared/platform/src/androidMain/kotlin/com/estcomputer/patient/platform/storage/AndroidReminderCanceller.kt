package com.estcomputer.patient.platform.storage

import android.app.NotificationManager
import android.content.Context
import com.estcomputer.patient.domain.port.ReminderCanceller

/** Cancels shown notifications. Cancelling scheduled alarms arrives with the reminders phase. */
public class AndroidReminderCanceller(context: Context) : ReminderCanceller {
    private val notifications = context.getSystemService(NotificationManager::class.java)

    override suspend fun cancelAll() {
        notifications?.cancelAll()
    }
}
