package com.estcomputer.patient.platform.boot

import android.content.ContentResolver
import android.os.SystemClock
import android.provider.Settings
import com.estcomputer.patient.domain.port.BootInfo

public class AndroidBootInfo(private val resolver: ContentResolver) : BootInfo {

    // The setting exists on every supported API level; the fallback only keeps a vendor quirk from crashing an unlock.
    override fun bootCount(): Int = Settings.Global.getInt(resolver, Settings.Global.BOOT_COUNT, UNKNOWN_BOOT_COUNT)

    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()

    private companion object {
        const val UNKNOWN_BOOT_COUNT = -1
    }
}
