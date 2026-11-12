package com.estcomputer.patient.platform.screen

import android.app.Activity
import android.os.Build
import android.view.Window
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal interface SecureWindow {
    fun setSecure(isSecure: Boolean)

    /** Keeps the system from taking a recents screenshot. Does nothing where the platform cannot. */
    fun hideRecentsPreview()
}

internal class ActivitySecureWindow(private val activity: Activity) : SecureWindow {
    override fun setSecure(isSecure: Boolean) = setSecureFlag(activity.window, isSecure)

    override fun hideRecentsPreview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) activity.setRecentsScreenshotEnabled(false)
    }
}

internal fun setSecureFlag(window: Window, isSecure: Boolean) {
    if (isSecure) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

/** Applies [ScreenSecurityPolicy] to one window. The activity calls the lifecycle members. */
public class ScreenSecurityController internal constructor(
    private val policy: ScreenSecurityPolicy,
    private val window: SecureWindow,
    private val sdkInt: Int,
) {
    private var isPausedWithoutRecentsApi = false

    public constructor(activity: Activity, policy: ScreenSecurityPolicy) :
        this(policy, ActivitySecureWindow(activity), Build.VERSION.SDK_INT)

    /** Call from `onCreate` before `setContent`, so no frame is ever shown without the flag. */
    public fun applyOnCreate() {
        apply(policy.isSecureRequired.value)
        window.hideRecentsPreview()
    }

    /** Applies later changes of the policy. Collect on the main dispatcher, for example `lifecycleScope`. */
    public fun observe(scope: CoroutineScope): Job =
        scope.launch { policy.isSecureRequired.collect { apply(it) } }

    // Before API 33 the recents screenshot is taken at pause, so the flag must be on until resume.
    public fun onPause() {
        if (sdkInt >= RECENTS_SCREENSHOT_API) return
        isPausedWithoutRecentsApi = true
        apply(policy.isSecureRequired.value)
    }

    public fun onResume() {
        if (sdkInt >= RECENTS_SCREENSHOT_API) return
        isPausedWithoutRecentsApi = false
        apply(policy.isSecureRequired.value)
    }

    private fun apply(isRequired: Boolean) {
        window.setSecure(isRequired || isPausedWithoutRecentsApi)
    }

    private companion object {
        const val RECENTS_SCREENSHOT_API = Build.VERSION_CODES.TIRAMISU
    }
}
