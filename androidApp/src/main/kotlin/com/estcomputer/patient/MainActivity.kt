package com.estcomputer.patient

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.estcomputer.patient.designsystem.theme.PatientMobileTheme
import com.estcomputer.patient.runtime.PatientAppHost
import com.estcomputer.patient.runtime.PatientRuntime
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val runtime: PatientRuntime by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runtime.attachScreenSecurity(this)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            PatientMobileTheme {
                PatientAppHost(runtime) {
                    PatientMobileApp()
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        runtime.onUserInteraction()
        return super.dispatchTouchEvent(event)
    }

    // Called for key presses and touch-downs. dispatchKeyEvent is restricted API on ComponentActivity.
    override fun onUserInteraction() {
        super.onUserInteraction()
        runtime.onUserInteraction()
    }
}
