package com.estcomputer.patient.domain.session

import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.util.suspendRunCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Owns the lock state and the scope in which every job that touches decrypted data must run.
 * The scope exists only while unlocked and is cancelled before anything else happens on lock.
 */
public class SessionCoordinator(
    private val applicationScope: CoroutineScope,
    private val keyManager: KeyManager,
    private val closers: List<SessionCloser>,
) {
    private val mutex = Mutex()
    private val _lockState = MutableStateFlow<LockState>(LockState.Locked(isRecoveryRequired = false))
    private val session = MutableStateFlow<Session?>(null)

    public val lockState: StateFlow<LockState> = _lockState.asStateFlow()

    /** The scope of the current unlocked session, or null while locked. A new one is made on every unlock. */
    public val unlockedSessionScope: CoroutineScope?
        get() = session.value?.scope

    /** Reports the state found at start-up, before any unlock. Ignored while a session is open. */
    public suspend fun setIdleState(state: LockState) {
        require(state !is LockState.Unlocked) { "use onUnlocked" }
        mutex.withLock {
            if (session.value == null) _lockState.value = state
        }
    }

    /** Opens a new session scope and publishes [LockState.Unlocked]. Call after the key manager unlocked. */
    public suspend fun onUnlocked() {
        mutex.withLock {
            session.value?.job?.cancelAndJoin()
            session.value = newSession()
            _lockState.value = LockState.Unlocked
        }
    }

    /** Cancels the session, closes dependants, then zeroes the keys. Safe to call repeatedly. */
    public suspend fun lock() {
        mutex.withLock {
            val open = session.value
            if (open == null) {
                // Keys may exist even though no session was announced, so zeroing is never skipped.
                withContext(NonCancellable) { keyManager.lock() }
            } else {
                lockOpenSession(open)
            }
        }
    }

    /** After a completed wipe: no vault exists any more. */
    public suspend fun onWiped() {
        lock()
        mutex.withLock { _lockState.value = LockState.Unconfigured }
    }

    private suspend fun lockOpenSession(open: Session) {
        try {
            open.job.cancelAndJoin()
            session.value = null
            _lockState.value = LockState.Locked(isRecoveryRequired = false)
            closers.forEach { closer -> suspendRunCatching { closer.close() } }
        } finally {
            // The keys are zeroed even when a closer fails or the caller is cancelled.
            withContext(NonCancellable) { keyManager.lock() }
        }
    }

    private fun newSession(): Session {
        val job = SupervisorJob(applicationScope.coroutineContext[Job])
        return Session(job, CoroutineScope(applicationScope.coroutineContext.minusKey(Job) + job))
    }

    private class Session(val job: Job, val scope: CoroutineScope)
}
