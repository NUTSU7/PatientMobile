package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.BiometricBeginResult
import com.estcomputer.patient.domain.model.BiometricEnrolmentResult
import com.estcomputer.patient.domain.model.UnlockResult
import com.estcomputer.patient.domain.port.BiometricChallenge
import com.estcomputer.patient.domain.port.BiometricKeyManager

/**
 * Biometric enrolment, unlock, refresh and disable. Any failure that leaves the key or blob unusable clears
 * both, so the patient falls back to the password and can enable biometrics again.
 */
internal class BiometricFlow(
    private val unlock: BiometricUnlockFlow,
    private val enrolment: BiometricEnrolmentFlow,
) : BiometricKeyManager {
    override suspend fun beginBiometricUnlock(): BiometricBeginResult = unlock.begin()

    override suspend fun completeBiometricUnlock(challenge: BiometricChallenge): UnlockResult =
        unlock.complete(challenge)

    override suspend fun beginBiometricEnrolment(): BiometricBeginResult = enrolment.begin()

    override suspend fun completeBiometricEnrolment(challenge: BiometricChallenge): BiometricEnrolmentResult =
        enrolment.complete(challenge)

    override suspend fun disableBiometrics() = enrolment.disable()
}
