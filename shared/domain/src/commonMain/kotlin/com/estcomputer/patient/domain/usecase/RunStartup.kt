package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.LockState
import com.estcomputer.patient.domain.model.StartupResult
import com.estcomputer.patient.domain.model.StartupState
import com.estcomputer.patient.domain.model.WipeResult
import com.estcomputer.patient.domain.model.WipeStepResult
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.port.TransientFiles
import com.estcomputer.patient.domain.session.SessionCoordinator

/**
 * Runs before any screen: finishes an interrupted wipe, empties the transient files, then publishes the
 * lock state. The state stays locked until this returns, so nothing is shown early.
 */
public class RunStartup(
    private val keyManager: KeyManager,
    private val wipeAllData: WipeAllData,
    private val transientFiles: TransientFiles,
    private val sessionCoordinator: SessionCoordinator,
) {
    public suspend operator fun invoke(): StartupResult {
        val state = settledState()
        val isCleanupComplete = transientFiles.clear() == WipeStepResult.DONE
        when (state) {
            StartupState.Clean -> sessionCoordinator.setIdleState(LockState.Unconfigured)
            StartupState.VaultPresent -> sessionCoordinator.setIdleState(LockState.Locked(isRecoveryRequired = false))
            // The coordinator already starts locked, so an unfinished wipe or orphaned data shows no vault.
            StartupState.WipeInterrupted, StartupState.OrphanedData -> Unit
        }
        return StartupResult(state, isCleanupComplete)
    }

    private suspend fun settledState(): StartupState {
        val state = keyManager.startupState()
        if (state != StartupState.WipeInterrupted) return state
        return if (wipeAllData() == WipeResult.DONE) keyManager.startupState() else StartupState.WipeInterrupted
    }
}
