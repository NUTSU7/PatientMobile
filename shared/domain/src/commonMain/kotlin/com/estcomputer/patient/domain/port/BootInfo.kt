package com.estcomputer.patient.domain.port

public interface BootInfo {
    /** Number of device boots; changes after every reboot. */
    public fun bootCount(): Int

    /** Monotonic milliseconds since boot, including deep sleep. */
    public fun elapsedRealtimeMillis(): Long
}
