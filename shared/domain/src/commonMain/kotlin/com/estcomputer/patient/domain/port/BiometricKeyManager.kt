package com.estcomputer.patient.domain.port

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.BiometricEnrolmentResult
import com.estcomputer.patient.domain.model.UnlockResult

/** Biometric half of key handling, split from [KeyManager]. `shared:crypto` implements both on one object. */
public interface BiometricKeyManager {
    /** Creates the decrypt challenge for the biometric prompt. */
    public suspend fun beginBiometricUnlock(): BiometricBeginResult

    /** Call after [BiometricAuthenticator.authenticate] succeeded for [challenge]. */
    public suspend fun completeBiometricUnlock(challenge: BiometricChallenge): UnlockResult

    /** Valid only while unlocked. Also used to refresh the stored biometric blob after a password unlock. */
    public suspend fun beginBiometricEnrolment(): BiometricBeginResult

    public suspend fun completeBiometricEnrolment(challenge: BiometricChallenge): BiometricEnrolmentResult

    /** Deletes the biometric key and blob. */
    public suspend fun disableBiometrics()
}
