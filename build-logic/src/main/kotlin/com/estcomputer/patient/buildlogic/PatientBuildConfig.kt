package com.estcomputer.patient.buildlogic

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal object PatientBuildConfig {
    const val MIN_SDK = 29
    const val COMPILE_SDK = 34
    const val NAMESPACE_ROOT = "com.estcomputer.patient"
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}
