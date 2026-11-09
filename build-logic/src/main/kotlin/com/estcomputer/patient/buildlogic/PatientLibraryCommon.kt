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
            withHostTest {}
            withDeviceTest {
                instrumentationRunner = INSTRUMENTATION_RUNNER
            }
        }
        sourceSets.getByName("commonTest").dependencies {
            implementation(kotlin("test"))
        }
    }
}

private const val ANDROID_LIBRARY_EXTENSION = "androidLibrary"
private const val DETEKT_CONFIG_PATH = "config/detekt/detekt.yml"
private const val INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

internal fun Project.configureDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file(DETEKT_CONFIG_PATH))
        source.setFrom(fileTree("src") { include("*/kotlin/**") })
    }
}
