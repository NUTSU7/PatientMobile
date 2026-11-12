package com.estcomputer.patient.runtime

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.BiometricEnrolmentResult
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BiometricChallenge
import com.estcomputer.patient.domain.port.BiometricKeyManager

/** Forwards to the biometric half of the key manager, which is built on first use like [DeferredKeyManager]. */
internal class DeferredBiometricKeyManager(private val target: suspend () -> BiometricKeyManager) :
    BiometricKeyManager {
    override suspend fun beginBiometricUnlock(): BiometricBeginResult = target().beginBiometricUnlock()

    override suspend fun completeBiometricUnlock(challenge: BiometricChallenge): UnlockResult =
        target().completeBiometricUnlock(challenge)

    override suspend fun beginBiometricEnrolment(): BiometricBeginResult = target().beginBiometricEnrolment()

    override suspend fun completeBiometricEnrolment(challenge: BiometricChallenge): BiometricEnrolmentResult =
        target().completeBiometricEnrolment(challenge)

    override suspend fun disableBiometrics() = target().disableBiometrics()
}
