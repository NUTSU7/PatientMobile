package com.estcomputer.patient.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyReleaseManifestTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mergedManifest: RegularFileProperty

    @TaskAction
    fun verify() {
        val violations = ManifestRules.violations(mergedManifest.get().asFile.readText())
        if (violations.isNotEmpty()) {
            throw GradleException("Merged release manifest failed:\n" + violations.joinToString("\n"))
        }
    }
}
