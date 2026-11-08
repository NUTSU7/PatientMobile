package com.estcomputer.patient.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class PatientMultiplatformLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.library")
            pluginManager.apply("patient.verification")

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {
                    compilerOptions {
                        jvmTarget.set(PatientBuildConfig.JVM_TARGET)
                    }
                }
                jvm {
                    compilerOptions {
                        jvmTarget.set(PatientBuildConfig.JVM_TARGET)
                    }
                }
                iosX64()
                iosArm64()
                iosSimulatorArm64()
                applyDefaultHierarchyTemplate()
            }

            configureAndroidLibrary()
            configureDetekt()
        }
    }
}
