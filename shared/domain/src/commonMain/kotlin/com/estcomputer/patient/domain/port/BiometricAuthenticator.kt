package com.estcomputer.patient.domain.port

public interface BiometricAuthenticator {
    /** Shows the system prompt for [challenge]; on [BiometricOutcome.SUCCEEDED] the challenge is ready to use. */
    public suspend fun authenticate(challenge: BiometricChallenge): BiometricOutcome
}

public enum class BiometricOutcome {
    SUCCEEDED,
    CANCELLED,
    LOCKED_OUT,
    UNAVAILABLE,
}
