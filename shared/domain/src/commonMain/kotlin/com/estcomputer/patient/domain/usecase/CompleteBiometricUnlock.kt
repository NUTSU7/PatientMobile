package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BiometricChallenge
import com.estcomputer.patient.domain.port.BiometricKeyManager
import com.estcomputer.patient.domain.session.SessionCoordinator

/** Call after the biometric prompt succeeded for [challenge]: opens the vault and the session. */
public class CompleteBiometricUnlock(
    private val biometricKeyManager: BiometricKeyManager,
    private val sessionCoordinator: SessionCoordinator,
) {
    public suspend operator fun invoke(challenge: BiometricChallenge): UnlockResult {
        val ticket = sessionCoordinator.startUnlock()
        val result = biometricKeyManager.completeBiometricUnlock(challenge)
        return if (result is UnlockResult.Unlocked && !sessionCoordinator.onUnlocked(ticket)) {
            UnlockResult.Failed(KeyError.LOCKED_DURING_UNLOCK)
        } else {
            result
        }
    }
}
