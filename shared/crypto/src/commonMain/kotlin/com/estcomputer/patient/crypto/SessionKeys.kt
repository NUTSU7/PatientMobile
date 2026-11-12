package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes
import kotlin.time.Instant

/** The master key while unlocked. Callers hold the vault key manager's mutex. */
internal class SessionKeys {
    private var masterKey: SecretBytes? = null

    /**
     * Monotonic time of the last unlock by password or recovery words, for the one-minute password-change
     * window. Null after a biometric unlock: the fingerprint alone never allows a password change.
     */
    var passwordUnlockElapsedMillis: Long? = null
        private set

    /** When the password was last entered; a biometric unlock keeps the value from its blob. */
    var passwordUnlockAt: Instant = Instant.fromEpochMilliseconds(0)
        private set

    val isUnlocked: Boolean
        get() = masterKey != null

    /** Takes ownership of [mk] and zeroes any key held before. [passwordElapsedMillis] is null without a password. */
    fun start(mk: SecretBytes, passwordUnlockAt: Instant, passwordElapsedMillis: Long?) {
        masterKey?.zero()
        masterKey = mk
        this.passwordUnlockAt = passwordUnlockAt
        passwordUnlockElapsedMillis = passwordElapsedMillis
    }

    /** The key stays owned by the session: do not zero it. */
    fun requireMk(): SecretBytes = checkNotNull(masterKey) { "vault is locked" }

    fun clear() {
        masterKey?.zero()
        masterKey = null
    }
}
