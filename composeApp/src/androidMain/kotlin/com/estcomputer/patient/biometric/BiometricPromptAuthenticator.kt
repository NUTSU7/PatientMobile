package com.estcomputer.patient.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.estcomputer.patient.crypto.KeystoreBiometricChallenge
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.port.BiometricAuthenticator
import com.estcomputer.patient.domain.port.BiometricChallenge
import com.estcomputer.patient.domain.port.BiometricOutcome
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Texts come from resources, resolved by the caller. */
internal class BiometricPromptTexts(val title: String, val negativeButton: String)

/**
 * Shows the system prompt for a challenge made by `shared:crypto`, with strong biometrics only.
 * [currentActivity] is asked on every call, so no activity is kept alive between prompts.
 */
internal class BiometricPromptAuthenticator(
    private val currentActivity: () -> FragmentActivity?,
    private val texts: BiometricPromptTexts,
    private val dispatchers: DispatcherProvider,
) : BiometricAuthenticator {

    override suspend fun authenticate(challenge: BiometricChallenge): BiometricOutcome =
        withContext(dispatchers.main) {
            val own = challenge as? KeystoreBiometricChallenge
            val activity = currentActivity()
            if (own == null || activity == null) BiometricOutcome.UNAVAILABLE else prompt(activity, own)
        }

    private suspend fun prompt(activity: FragmentActivity, challenge: KeystoreBiometricChallenge): BiometricOutcome =
        suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) continuation.resume(BiometricOutcome.SUCCEEDED)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (continuation.isActive) continuation.resume(outcomeOfError(errorCode))
                }

                // A rejected finger is not terminal: the prompt stays open and the patient can try again.
                override fun onAuthenticationFailed() = Unit
            }
            val prompt = BiometricPrompt(activity, executor, callback)
            continuation.invokeOnCancellation { executor.execute { prompt.cancelAuthentication() } }
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(texts.title)
                .setNegativeButtonText(texts.negativeButton)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
            prompt.authenticate(info, BiometricPrompt.CryptoObject(challenge.cipher))
        }

    internal companion object {
        fun outcomeOfError(errorCode: Int): BiometricOutcome =
            when (errorCode) {
                BiometricPrompt.ERROR_USER_CANCELED,
                BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                BiometricPrompt.ERROR_CANCELED,
                -> BiometricOutcome.CANCELLED
                BiometricPrompt.ERROR_LOCKOUT,
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
                -> BiometricOutcome.LOCKED_OUT
                else -> BiometricOutcome.UNAVAILABLE
            }
    }
}
