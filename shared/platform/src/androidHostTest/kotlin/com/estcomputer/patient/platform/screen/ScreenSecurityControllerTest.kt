package com.estcomputer.patient.platform.screen

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeSecureWindow : SecureWindow {
    var isSecure: Boolean? = null
    var recentsHidden = 0

    override fun setSecure(isSecure: Boolean) {
        this.isSecure = isSecure
    }

    override fun hideRecentsPreview() {
        recentsHidden++
    }
}

private const val API_32 = 32
private const val API_33 = 33

class ScreenSecurityControllerTest {

    private val window = FakeSecureWindow()

    private fun controller(policy: ScreenSecurityPolicy, sdkInt: Int) =
        ScreenSecurityController(policy, window, sdkInt)

    @Test
    fun applyOnCreate_withDefaultPolicy_setsTheFlagAndHidesRecents() {
        controller(ScreenSecurityPolicy(), API_33).applyOnCreate()

        assertEquals(true, window.isSecure)
        assertEquals(1, window.recentsHidden)
    }

    @Test
    fun applyOnCreate_whenScreenshotsAllowed_leavesTheFlagOffButStillHidesRecents() {
        controller(ScreenSecurityPolicy(isScreenshotAllowed = true), API_33).applyOnCreate()

        assertEquals(false, window.isSecure)
        assertEquals(1, window.recentsHidden)
    }

    @Test
    fun observe_followsForceSecure() = runTest {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        val controller = controller(policy, API_33)
        controller.applyOnCreate()
        val job = controller.observe(kotlinx.coroutines.CoroutineScope(StandardTestDispatcher(testScheduler)))
        runCurrent()

        policy.forceSecure("words")
        runCurrent()
        assertEquals(true, window.isSecure)

        policy.release("words")
        runCurrent()
        assertEquals(false, window.isSecure)
        job.cancel()
    }

    @Test
    fun onPause_beforeApi33_addsTheFlagEvenWhenScreenshotsAreAllowed() {
        val controller = controller(ScreenSecurityPolicy(isScreenshotAllowed = true), API_32)
        controller.applyOnCreate()

        controller.onPause()

        assertEquals(true, window.isSecure)
    }

    @Test
    fun onResume_beforeApi33_removesTheFlagWhenScreenshotsAreAllowed() {
        val controller = controller(ScreenSecurityPolicy(isScreenshotAllowed = true), API_32)
        controller.applyOnCreate()
        controller.onPause()

        controller.onResume()

        assertEquals(false, window.isSecure)
    }

    @Test
    fun onResume_beforeApi33_keepsTheFlagWhenForced() {
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        val controller = controller(policy, API_32)
        controller.applyOnCreate()
        policy.forceSecure("words")
        controller.onPause()

        controller.onResume()

        assertEquals(true, window.isSecure)
    }

    @Test
    fun policyChange_whilePausedBeforeApi33_doesNotDropTheFlag() = runTest {
        val policy = ScreenSecurityPolicy()
        val controller = controller(policy, API_32)
        controller.applyOnCreate()
        val job = controller.observe(kotlinx.coroutines.CoroutineScope(StandardTestDispatcher(testScheduler)))
        controller.onPause()

        policy.setScreenshotAllowed(true)
        runCurrent()

        assertEquals(true, window.isSecure)
        job.cancel()
    }

    @Test
    fun onPause_onApi33_doesNothing() {
        val controller = controller(ScreenSecurityPolicy(isScreenshotAllowed = true), API_33)
        controller.applyOnCreate()

        controller.onPause()
        assertFalse(window.isSecure == true)

        controller.onResume()
        assertTrue(window.isSecure == false)
    }
}
