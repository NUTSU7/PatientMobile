package com.estcomputer.patient.platform.integrity

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSources(
    var existing: Set<String> = emptySet(),
    override var buildTags: String? = "release-keys",
    override var buildType: String? = "user",
    override var isAppDebuggable: Boolean = false,
    var isDebugger: Boolean = false,
) : IntegritySources {
    override fun fileExists(path: String): Boolean = path in existing
    override fun isDebuggerConnected(): Boolean = isDebugger
}

class IntegrityTest {

    private val clean = FakeSources()

    @Test
    fun evaluate_onCleanDevice_reportsNothing() {
        val report = IntegrityRules.evaluate(clean)

        assertEquals(IntegrityReport(false, false, false), report)
        assertFalse(report.hasConcern)
    }

    @Test
    fun evaluate_withSuBinary_suspectsRoot() {
        assertTrue(IntegrityRules.evaluate(FakeSources(existing = setOf("/system/xbin/su"))).isRootSuspected)
    }

    @Test
    fun evaluate_withMagiskPath_suspectsRoot() {
        assertTrue(IntegrityRules.evaluate(FakeSources(existing = setOf("/sbin/.magisk"))).isRootSuspected)
    }

    @Test
    fun evaluate_withTestKeys_suspectsRoot() {
        assertTrue(IntegrityRules.evaluate(FakeSources(buildTags = "dev-keys,test-keys")).isRootSuspected)
    }

    @Test
    fun evaluate_withEngOrUserdebugBuild_suspectsRoot() {
        assertTrue(IntegrityRules.evaluate(FakeSources(buildType = "eng")).isRootSuspected)
        assertTrue(IntegrityRules.evaluate(FakeSources(buildType = "userdebug")).isRootSuspected)
    }

    @Test
    fun evaluate_withMissingBuildProperties_doesNotSuspectRoot() {
        assertFalse(IntegrityRules.evaluate(FakeSources(buildTags = null, buildType = null)).isRootSuspected)
    }

    @Test
    fun evaluate_withDebuggableApp_reportsDebuggable() {
        val report = IntegrityRules.evaluate(FakeSources(isAppDebuggable = true))

        assertTrue(report.isDebuggable)
        assertFalse(report.isRootSuspected)
    }

    @Test
    fun evaluate_withDebuggerAttached_reportsIt() {
        assertTrue(IntegrityRules.evaluate(FakeSources(isDebugger = true)).isDebuggerAttached)
    }

    @Test
    fun evaluate_withUnrelatedFiles_isClean() {
        assertFalse(IntegrityRules.evaluate(FakeSources(existing = setOf("/system/bin/sh"))).hasConcern)
    }

    private class CountingChecker(private val report: IntegrityReport) : DeviceIntegrityChecker {
        var calls = 0
        override suspend fun check(): IntegrityReport {
            calls++
            return report
        }
    }

    @Test
    fun reportToShow_whenConcern_returnsItOnlyOnce() = runTest {
        val report = IntegrityReport(isRootSuspected = true, isDebuggable = false, isDebuggerAttached = false)
        val checker = CountingChecker(report)
        val policy = IntegrityWarningPolicy(checker)

        assertEquals(report, policy.reportToShow())
        assertNull(policy.reportToShow())
        assertEquals(1, checker.calls)
    }

    @Test
    fun reportToShow_whenClean_returnsNullAndChecksAgainNextTime() = runTest {
        val checker = CountingChecker(IntegrityReport(false, false, false))
        val policy = IntegrityWarningPolicy(checker)

        assertNull(policy.reportToShow())
        assertNull(policy.reportToShow())
        assertEquals(2, checker.calls)
    }
}
