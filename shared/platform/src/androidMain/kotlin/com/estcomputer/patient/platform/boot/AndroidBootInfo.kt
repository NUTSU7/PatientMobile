package com.estcomputer.patient.platform.boot

import android.content.ContentResolver
import android.os.SystemClock
import android.provider.Settings
import com.estcomputer.patient.domain.port.BootInfo
import kotlin.random.Random

public class AndroidBootInfo internal constructor(private val readBootCount: () -> Int?) : BootInfo {

    public constructor(resolver: ContentResolver) : this({
        Settings.Global.getInt(resolver, Settings.Global.BOOT_COUNT, UNKNOWN_BOOT_COUNT).takeIf { it >= 0 }
    })

    // Fails closed: without the platform count every process start looks like a reboot, so the biometric
    // unlock asks for the password and the lockout re-arms. A fixed fallback would hide real reboots.
    private val processBootCount: Int by lazy { Random.nextInt(FALLBACK_MIN, Int.MAX_VALUE) }

    override fun bootCount(): Int = readBootCount() ?: processBootCount

    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()

    private companion object {
        const val UNKNOWN_BOOT_COUNT = -1

        // Far above any real boot count, and never negative, which the vault would reject as corrupt.
        const val FALLBACK_MIN = 1_000_000_000
    }
}
