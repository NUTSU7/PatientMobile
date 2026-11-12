package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.BootInfo
import kotlin.time.Clock

internal class BioPolicy(
    private val bootInfo: BootInfo,
    private val clock: Clock,
) {
    /**
     * True when the patient must enter the password again: after a reboot, after the maximum age, or when
     * the wall clock moved back before the recorded password unlock.
     */
    fun requiresPassword(policy: BioPolicyData): Boolean {
        val now = clock.now()
        return policy.bootCount != bootInfo.bootCount() ||
            policy.passwordUnlockAt > now ||
            now - policy.passwordUnlockAt > CryptoConstants.BIO_PASSWORD_MAX_AGE
    }
}
