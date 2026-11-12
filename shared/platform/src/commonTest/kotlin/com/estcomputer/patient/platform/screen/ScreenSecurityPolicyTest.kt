package com.estcomputer.patient.platform.screen

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenSecurityPolicyTest {

    @Test
    fun isSecureRequired_byDefault_isTrue() {
        assertTrue(ScreenSecurityPolicy().isSecureRequired.value)
    }

    @Test
    fun isSecureRequired_whenScreenshotsAllowed_isFalse() {
        assertFalse(ScreenSecurityPolicy(isScreenshotAllowed = true).isSecureRequired.value)
    }

    @Test
    fun setScreenshotAllowed_togglesTheState() {
        val policy = ScreenSecurityPolicy()

        policy.setScreenshotAllowed(true)
        assertFalse(policy.isSecureRequired.value)

        policy.setScreenshotAllowed(false)
        assertTrue(policy.isSecureRequired.value)
    }

    @Test
    fun forceSecure_overridesAllowedScreenshots() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)

        policy.forceSecure("words")

        assertTrue(policy.isSecureRequired.value)
    }

    @Test
    fun release_afterForce_returnsToTheSetting() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        policy.forceSecure("words")

        policy.release("words")

        assertFalse(policy.isSecureRequired.value)
    }

    @Test
    fun nestedForce_staysSecureUntilEveryCallIsReleased() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        policy.forceSecure("words")
        policy.forceSecure("words")

        policy.release("words")
        assertTrue(policy.isSecureRequired.value)

        policy.release("words")
        assertFalse(policy.isSecureRequired.value)
    }

    @Test
    fun differentTokens_areIndependent() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        policy.forceSecure("words")
        policy.forceSecure("export")

        policy.release("words")

        assertTrue(policy.isSecureRequired.value)
    }

    @Test
    fun release_ofUnknownToken_doesNothing() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)

        policy.release("never-forced")

        assertFalse(policy.isSecureRequired.value)
    }

    @Test
    fun release_ofUnknownToken_doesNotCancelARealForce() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        policy.forceSecure("words")

        policy.release("other")

        assertTrue(policy.isSecureRequired.value)
    }

    @Test
    fun release_moreOftenThanForced_neverGoesNegative() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        policy.forceSecure("words")
        policy.release("words")
        policy.release("words")

        policy.forceSecure("words")

        assertTrue(policy.isSecureRequired.value)
    }

    @Test
    fun isSecureRequired_emitsOnlyChanges() = runTest {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)

        policy.isSecureRequired.test {
            assertEquals(false, awaitItem())
            policy.forceSecure("words")
            assertEquals(true, awaitItem())
            policy.forceSecure("words")
            policy.release("words")
            policy.release("words")
            assertEquals(false, awaitItem())
        }
    }
}
