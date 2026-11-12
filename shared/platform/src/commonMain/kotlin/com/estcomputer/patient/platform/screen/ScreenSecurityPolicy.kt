package com.estcomputer.patient.platform.screen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Decides whether the window must hide its content from screenshots and recordings. Screens that show
 * secrets, such as the recovery words, force it on, and that overrides the "screenshots allowed" setting.
 * Call on the main thread; the class keeps no locks.
 */
public class ScreenSecurityPolicy(isScreenshotAllowed: Boolean = false) {
    private var isScreenshotAllowed = isScreenshotAllowed
    private val forceCounts = mutableMapOf<String, Int>()
    private val _isSecureRequired = MutableStateFlow(computeRequired())

    public val isSecureRequired: StateFlow<Boolean> = _isSecureRequired.asStateFlow()

    public fun setScreenshotAllowed(isAllowed: Boolean) {
        isScreenshotAllowed = isAllowed
        publish()
    }

    /** Each call must be paired with one [release] of the same token; nested calls stack. */
    public fun forceSecure(token: String) {
        forceCounts[token] = (forceCounts[token] ?: 0) + 1
        publish()
    }

    /** Releasing a token that was never forced does nothing. */
    public fun release(token: String) {
        val count = forceCounts[token] ?: return
        if (count <= 1) forceCounts.remove(token) else forceCounts[token] = count - 1
        publish()
    }

    private fun computeRequired(): Boolean = !isScreenshotAllowed || forceCounts.isNotEmpty()

    private fun publish() {
        _isSecureRequired.value = computeRequired()
    }
}
