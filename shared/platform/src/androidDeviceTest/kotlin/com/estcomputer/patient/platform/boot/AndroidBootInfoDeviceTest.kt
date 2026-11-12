package com.estcomputer.patient.platform.boot

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AndroidBootInfoDeviceTest {
    private val info = AndroidBootInfo(InstrumentationRegistry.getInstrumentation().targetContext.contentResolver)

    @Test
    fun elapsedRealtimeMillis_isPositiveAndNeverGoesBackwards() {
        val first = info.elapsedRealtimeMillis()
        val second = info.elapsedRealtimeMillis()

        assertTrue(first > 0)
        assertTrue(second >= first)
    }

    @Test
    fun bootCount_readsTheSameValueTwiceInOneBoot() {
        assertEquals(info.bootCount(), info.bootCount())
        assertTrue(info.bootCount() >= 0, "the platform setting must be readable")
    }
}
