package com.estcomputer.patient.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class PatientAndroidJvmLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("patient.verification")

            extensions.configure<KotlinMultiplatformExtension> {
                jvm {
                    compilerOptions {
                        jvmTarget.set(PatientBuildConfig.JVM_TARGET)
                    }
                }
                applyDefaultHierarchyTemplate()
            }

            configureAndroidLibrary()
            configureDetekt()
        }
    }
}
