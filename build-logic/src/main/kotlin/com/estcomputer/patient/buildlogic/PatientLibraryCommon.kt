package com.estcomputer.patient.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureAndroidLibrary() {
    extensions.configure<KotlinMultiplatformExtension> {
        extensions.configure<KotlinMultiplatformAndroidLibraryTarget>(ANDROID_LIBRARY_EXTENSION) {
            namespace = "${PatientBuildConfig.NAMESPACE_ROOT}.${project.name}"
            compileSdk = PatientBuildConfig.COMPILE_SDK
            minSdk = PatientBuildConfig.MIN_SDK
            compilerOptions {
                jvmTarget.set(PatientBuildConfig.JVM_TARGET)
            }
        }
    }
}

private const val ANDROID_LIBRARY_EXTENSION = "androidLibrary"

internal fun Project.configureDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        source.setFrom(fileTree("src") { include("*/kotlin/**") })
    }
}
