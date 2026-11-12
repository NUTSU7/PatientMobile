package com.estcomputer.patient.platform.autolock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Turns process lifecycle and screen-off broadcasts into [AutoLockPolicy] events.
 * [ProcessLifecycleOwner] already waits about 700 ms before reporting a stop and ignores configuration
 * changes, so there is no second debounce here.
 */
public class AndroidAutoLockEvents(
    private val context: Context,
    private val policy: AutoLockPolicy,
) {
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) policy.onScreenOff()
        }
    }

    private val observer = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) = registerScreenOffReceiver()
        override fun onStart(owner: LifecycleOwner) = policy.onForeground()
        override fun onStop(owner: LifecycleOwner) = policy.onBackground()
        override fun onDestroy(owner: LifecycleOwner) = context.unregisterReceiver(screenOffReceiver)
    }

    /** Call once from `Application.onCreate`, on the main thread. */
    public fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    private fun registerScreenOffReceiver() {
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        // ACTION_SCREEN_OFF is protected, so only the system can send it; the flag only exists from API 33.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(screenOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(screenOffReceiver, filter)
        }
    }
}
