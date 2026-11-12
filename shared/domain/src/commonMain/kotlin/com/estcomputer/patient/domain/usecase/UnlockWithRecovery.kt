package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.session.SessionCoordinator

/** Sets a new password with the recovery words and opens the session. The caller zeroes both inputs. */
public class UnlockWithRecovery(
    private val keyManager: KeyManager,
    private val sessionCoordinator: SessionCoordinator,
) {
    public suspend operator fun invoke(words: SecretChars, newPassword: SecretChars): UnlockResult {
        val ticket = sessionCoordinator.startUnlock()
        val result = keyManager.unlockWithRecovery(words, newPassword)
        return if (result is UnlockResult.Unlocked && !sessionCoordinator.onUnlocked(ticket)) {
            UnlockResult.Failed(KeyError.LOCKED_DURING_UNLOCK)
        } else {
            result
        }
    }
}
