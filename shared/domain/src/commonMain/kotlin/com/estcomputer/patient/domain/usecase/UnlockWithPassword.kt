package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.session.SessionCoordinator

/** Unlocks with the password and opens the session. The caller zeroes [password]. */
public class UnlockWithPassword(
    private val keyManager: KeyManager,
    private val sessionCoordinator: SessionCoordinator,
) {
    public suspend operator fun invoke(password: SecretChars): UnlockResult {
        val ticket = sessionCoordinator.startUnlock()
        val result = keyManager.unlockWithPassword(password)
        return if (result is UnlockResult.Unlocked && !sessionCoordinator.onUnlocked(ticket)) {
            UnlockResult.Failed(KeyError.LOCKED_DURING_UNLOCK)
        } else {
            result
        }
    }
}
