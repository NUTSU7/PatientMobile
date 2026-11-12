package com.estcomputer.patient.platform.autolock

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.port.BootInfo
import com.estcomputer.patient.domain.session.SessionCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Decides when the app locks itself. Every time is measured on the monotonic clock, so changing the wall
 * clock changes nothing. All members must be called on the main dispatcher; the class keeps no locks.
 */
public class AutoLockPolicy(
    private val sessionCoordinator: SessionCoordinator,
    private val bootInfo: BootInfo,
    private val dispatchers: DispatcherProvider,
    private val scope: CoroutineScope,
    settings: AutoLockSettings = AutoLockSettings(),
) {
    private val deadlines = LockDeadlines(settings.clamped())
    private var timer: Job? = null
    private var isStarted = false

    /** Starts following the lock state: a new unlock begins a foreground session. Call once at start-up. */
    public fun start() {
        if (isStarted) return
        isStarted = true
        scope.launch(dispatchers.main) {
            sessionCoordinator.lockState.collect { state ->
                if (state == LockState.Unlocked) beginSession() else timer?.cancel()
            }
        }
    }

    public fun onUserInteraction() {
        if (deadlines.isBackgrounded) return
        deadlines.lastInteractionAt = now()
        // The running timer re-reads the deadline when it fires, so a touch never needs a new coroutine.
        if (timer?.isActive != true) reschedule()
    }

    public fun onBackground() {
        if (!deadlines.isBackgrounded) deadlines.backgroundedAt = now()
        reschedule()
    }

    /** The process may have been frozen while away, so the elapsed time is checked here as well as by the timer. */
    public fun onForeground() {
        if (!deadlines.isBackgrounded) return
        if (deadlines.millisUntilLock(now()) <= 0) {
            lockNow()
        } else {
            deadlines.backgroundedAt = null
            deadlines.lastInteractionAt = now()
            reschedule()
        }
    }

    public fun onScreenOff() {
        lockNow()
    }

    public fun setExternalActivityInProgress(isInProgress: Boolean) {
        deadlines.externalActivitySetAt = if (isInProgress) now() else null
        if (deadlines.isBackgrounded && deadlines.millisUntilLock(now()) <= 0) lockNow() else reschedule()
    }

    private fun beginSession() {
        deadlines.beginForegroundSession(now())
        reschedule()
    }

    private fun reschedule() {
        timer?.cancel()
        if (sessionCoordinator.lockState.value != LockState.Unlocked) return
        val remaining = deadlines.millisUntilLock(now())
        timer = scope.launch(dispatchers.main) {
            delay(remaining)
            if (deadlines.millisUntilLock(now()) <= 0) lockNow() else reschedule()
        }
    }

    private fun lockNow() {
        timer?.cancel()
        scope.launch(dispatchers.main) { sessionCoordinator.lock() }
    }

    private fun now(): Long = bootInfo.elapsedRealtimeMillis()
}
