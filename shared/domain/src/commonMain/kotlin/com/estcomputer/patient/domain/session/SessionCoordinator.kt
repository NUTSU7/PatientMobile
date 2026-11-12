package com.estcomputer.patient.domain.session

import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.util.suspendRunCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

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
    private var lockCount = 0L

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

    /** Call before asking the key manager to unlock, and pass the ticket to [onUnlocked] afterwards. */
    public suspend fun startUnlock(): UnlockTicket = mutex.withLock { UnlockTicket(lockCount) }

    /**
     * Opens a new session scope and publishes [LockState.Unlocked]. Call after the key manager unlocked.
     * Returns false, and zeroes the keys again, when a lock arrived after [ticket] was taken: the patient
     * locked the app while the unlock was running, so no session opens.
     */
    public suspend fun onUnlocked(ticket: UnlockTicket): Boolean =
        mutex.withLock {
            if (ticket.lockCount != lockCount) {
                withContext(NonCancellable) { keyManager.lock() }
                // The unlock or setup succeeded, so a vault exists even if the state still says Unconfigured.
                _lockState.value = LockState.Locked(isRecoveryRequired = false)
                false
            } else {
                session.value?.job?.cancelAndAwait()
                session.value = newSession()
                _lockState.value = LockState.Unlocked
                true
            }
        }

    /** Cancels the session, closes dependants, then zeroes the keys. Safe to call repeatedly. */
    public suspend fun lock() {
        mutex.withLock {
            lockCount++
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
            open.job.cancelAndAwait()
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

    private companion object {
        val JOIN_TIMEOUT = 3.seconds
    }

    // A job that ignores cancellation (a blocking call, Argon2) must not keep the keys alive: after the timeout
    // the lock goes on and the closers make the job's next use of the data fail.
    private suspend fun Job.cancelAndAwait() {
        cancel()
        withTimeoutOrNull(JOIN_TIMEOUT) { join() }
    }
}
