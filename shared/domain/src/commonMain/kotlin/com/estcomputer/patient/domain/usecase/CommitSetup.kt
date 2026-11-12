package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.PendingSetup
import com.estcomputer.patient.domain.model.SetupCommitResult
import com.estcomputer.patient.domain.session.SessionCoordinator

/**
 * Writes the vault once the patient confirmed the recovery words, then opens the session. On
 * [KeyError.LOCKED_DURING_UNLOCK] the vault is written and the app is locked: the patient unlocks with the password.
 */
public class CommitSetup(private val sessionCoordinator: SessionCoordinator) {
    public suspend operator fun invoke(pending: PendingSetup): SetupCommitResult {
        val ticket = sessionCoordinator.startUnlock()
        val result = pending.commit()
        return if (result is SetupCommitResult.Committed && !sessionCoordinator.onUnlocked(ticket)) {
            SetupCommitResult.Failed(KeyError.LOCKED_DURING_UNLOCK)
        } else {
            result
        }
    }
}
