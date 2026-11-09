package com.estcomputer.patient.domain.port

import com.estcomputer.patient.domain.model.ChangePasswordResult
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.SetupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.UnlockResult

/**
 * The only way to reach key material. Implemented in `shared:crypto`.
 * Passwords and words passed in are borrowed: the caller zeroes them.
 */
public interface KeyManager {
    /** Inspects disk once at launch and reports how the app must start. */
    public suspend fun startupState(): StartupState

    /** Valid only in [StartupState.Clean]. Builds the keys in memory and returns the recovery words. */
    public suspend fun prepareSetup(password: SecretChars): SetupResult

    /** Counts a failed attempt only after verification failed; hardware-key failures are never counted. */
    public suspend fun unlockWithPassword(password: SecretChars): UnlockResult

    /** Replaces the password using the recovery words. Wrong words never count toward the auto-wipe. */
    public suspend fun unlockWithRecovery(words: SecretChars, newPassword: SecretChars): UnlockResult

    /** [old] may be null only within one minute of an unlock. */
    public suspend fun changePassword(old: SecretChars?, new: SecretChars): ChangePasswordResult

    /** A fresh copy of the database key; the caller zeroes it. Throws [IllegalStateException] while locked. */
    public suspend fun databaseKey(): SecretBytes

    /** A fresh copy of the file key-encryption key; the caller zeroes it. Throws [IllegalStateException] if locked. */
    public suspend fun fileKeyEncryptionKey(): SecretBytes

    /** Zeroes every key held in memory. Idempotent. */
    public suspend fun lock()

    /** Deletes the Keystore keys and the vault. Part of the wipe: keys go before files. */
    public suspend fun destroyKeys()

    /** [attempts] must be in 10..20, or null to turn the auto-wipe off. */
    public suspend fun setWipeAfterFailedAttempts(attempts: Int?)
}
