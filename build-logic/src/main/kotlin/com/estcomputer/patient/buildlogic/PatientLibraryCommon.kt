package com.estcomputer.patient.buildlogic

import com.android.build.api.dsl.LibraryExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configureAndroidLibrary() {
    extensions.configure<LibraryExtension> {
        namespace = "${PatientBuildConfig.NAMESPACE_ROOT}.${project.name}"
        compileSdk = PatientBuildConfig.COMPILE_SDK
        defaultConfig {
            minSdk = PatientBuildConfig.MIN_SDK
        }
        compileOptions {
            sourceCompatibility = PatientBuildConfig.JAVA_VERSION
            targetCompatibility = PatientBuildConfig.JAVA_VERSION
        }
    }
}

internal fun Project.configureDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        source.setFrom(fileTree("src") { include("*/kotlin/**") })
    }
}
