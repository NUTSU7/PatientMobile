package com.estcomputer.patient.platform.boot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AndroidBootInfoHostTest {

    @Test
    fun bootCount_whenReadable_returnsPlatformValue() {
        assertEquals(412, AndroidBootInfo { 412 }.bootCount())
    }

    @Test
    fun bootCount_whenUnreadable_isNonNegativeAndStableInOneProcess() {
        val info = AndroidBootInfo { null }

        val first = info.bootCount()

        assertTrue(first >= 0)
        assertEquals(first, info.bootCount())
    }

    @Test
    fun bootCount_whenUnreadable_isAboveAnyRealCount() {
        assertTrue(AndroidBootInfo { null }.bootCount() >= 1_000_000_000)
    }
}
