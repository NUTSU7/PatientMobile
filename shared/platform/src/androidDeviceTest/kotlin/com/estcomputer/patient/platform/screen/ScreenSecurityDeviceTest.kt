package com.estcomputer.patient.platform.screen

import android.app.Dialog
import android.view.Window
import android.view.WindowManager
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import kotlin.test.assertEquals

class ScreenSecurityDeviceTest {

    private class WindowOnly(private val window: Window) : SecureWindow {
        override fun setSecure(isSecure: Boolean) = setSecureFlag(window, isSecure)
        override fun hideRecentsPreview() = Unit
    }

    private fun Window.isSecureFlagSet(): Boolean =
        attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0

    private fun onWindow(test: (Window) -> Unit) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val dialog = Dialog(instrumentation.targetContext)
            test(requireNotNull(dialog.window))
        }
    }

    @Test
    fun applyOnCreate_setsFlagSecureOnTheWindow() = onWindow { window ->
        ScreenSecurityController(ScreenSecurityPolicy(), WindowOnly(window), sdkInt = 34).applyOnCreate()

        assertEquals(true, window.isSecureFlagSet())
    }

    @Test
    fun forceSecure_holdsWithScreenshotsAllowed() = onWindow { window ->
        val policy = ScreenSecurityPolicy(isScreenshotAllowed = true)
        val controller = ScreenSecurityController(policy, WindowOnly(window), sdkInt = 34)
        controller.applyOnCreate()
        assertEquals(false, window.isSecureFlagSet())

        policy.forceSecure("words")
        controller.applyOnCreate()

        assertEquals(true, window.isSecureFlagSet())
    }
}
