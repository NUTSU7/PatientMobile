package com.estcomputer.patient.buildlogic

import org.junit.Assert.assertEquals
import org.junit.Test

class ForbiddenDependencyRulesTest {

    @Test
    fun violations_withForbiddenGroups_reportsEach() {
        val forbidden = listOf(
            "com.google.firebase:firebase-crashlytics",
            "com.google.android.datatransport:transport-backend-cct",
            "com.google.android.gms:play-services-mlkit-document-scanner",
            "com.google.android.gms:play-services-analytics",
            "com.squareup.okhttp3:okhttp",
            "com.squareup.retrofit2:retrofit",
            "io.ktor:ktor-client-core",
            "com.android.volley:volley",
            "com.itextpdf:itext7-core",
            "io.sentry:sentry-android",
        )
        assertEquals(forbidden, ForbiddenDependencyRules.violations(forbidden))
    }

    @Test
    fun violations_withAllowedDependencies_isEmpty() {
        val allowed = listOf(
            "com.google.mlkit:text-recognition",
            "net.zetetic:sqlcipher-android",
            "io.ktor:ktor-serialization-kotlinx-json",
            "org.jetbrains.kotlinx:kotlinx-coroutines-core",
        )
        assertEquals(emptyList<String>(), ForbiddenDependencyRules.violations(allowed))
    }
}
