package com.estcomputer.patient.runtime

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.estcomputer.patient.domain.usecase.RunStartup
import com.estcomputer.patient.platform.autolock.AndroidAutoLockEvents
import com.estcomputer.patient.platform.autolock.AutoLockPolicy
import com.estcomputer.patient.platform.screen.ScreenSecurityController
import com.estcomputer.patient.platform.screen.ScreenSecurityPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * What the Android app module needs from the shared layers: start-up, user activity and screen security.
 * Everything else stays behind it, so `androidApp` depends on `composeApp` only.
 */
public class PatientRuntime internal constructor(
    private val applicationScope: CoroutineScope,
    private val runStartup: RunStartup,
    private val autoLockPolicy: AutoLockPolicy,
    private val autoLockEvents: AndroidAutoLockEvents,
    private val screenSecurityPolicy: ScreenSecurityPolicy,
) {
    private val _isStartupComplete = MutableStateFlow(false)

    /** False until an interrupted wipe is finished and the transient files are gone. No screen shows before. */
    public val isStartupComplete: StateFlow<Boolean> = _isStartupComplete.asStateFlow()

    /** Call once from `Application.onCreate`, on the main thread. */
    public fun start() {
        autoLockEvents.register()
        autoLockPolicy.start()
        applicationScope.launch {
            runStartup()
            _isStartupComplete.value = true
        }
    }

    /** Forward from `dispatchTouchEvent` and `dispatchKeyEvent`. */
    public fun onUserInteraction() {
        autoLockPolicy.onUserInteraction()
    }

    /** Call from `onCreate` before `setContent`, so no frame is shown without the secure flag. */
    public fun attachScreenSecurity(activity: ComponentActivity) {
        val controller = ScreenSecurityController(activity, screenSecurityPolicy)
        controller.applyOnCreate()
        controller.observe(activity.lifecycleScope)
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) = controller.onResume()
                override fun onPause(owner: LifecycleOwner) = controller.onPause()
            },
        )
    }
}
