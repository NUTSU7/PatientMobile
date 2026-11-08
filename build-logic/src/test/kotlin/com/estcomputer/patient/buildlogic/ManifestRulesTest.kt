package com.estcomputer.patient.buildlogic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestRulesTest {

    private fun fixture(name: String): String =
        requireNotNull(javaClass.classLoader.getResource("manifests/$name")) { "missing fixture $name" }
            .readText()

    @Test
    fun violations_withCleanManifest_isEmpty() {
        assertEquals(emptyList<String>(), ManifestRules.violations(fixture("clean.xml")))
    }

    @Test
    fun violations_withInternetPermission_reportsIt() {
        val violations = ManifestRules.violations(fixture("internet.xml"))
        assertEquals(listOf("permission not allowed: android.permission.INTERNET"), violations)
    }

    @Test
    fun violations_withExtraExportedReceiver_reportsIt() {
        val violations = ManifestRules.violations(fixture("extra-exported-receiver.xml"))
        assertTrue(violations.any { it.contains("androidx.profileinstaller.ProfileInstallReceiver") })
        assertEquals(1, violations.size)
    }

    @Test
    fun violations_withAllowBackupMissing_reportsIt() {
        val violations = ManifestRules.violations(fixture("backup-missing.xml"))
        assertEquals(listOf("allowBackup must be \"false\""), violations)
    }
}
