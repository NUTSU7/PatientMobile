package com.estcomputer.patient.buildlogic

internal object ForbiddenDependencyRules {

    private class Rule(val groupPrefix: String, val artifactPrefix: String = "")

    private val rules = listOf(
        Rule("com.google.firebase"),
        Rule("com.google.android.datatransport"),
        Rule("com.google.android.gms", "play-services-mlkit"),
        Rule("com.google.android.gms", "play-services-analytics"),
        Rule("com.google.android.gms", "play-services-ads"),
        Rule("com.crashlytics"),
        Rule("io.sentry"),
        Rule("com.squareup.okhttp"),
        Rule("com.squareup.retrofit2"),
        Rule("io.ktor", "ktor-client-"),
        Rule("com.android.volley"),
        Rule("com.itextpdf"),
    )

    /** Input and output are `group:artifact` coordinates. */
    fun violations(coordinates: Collection<String>): List<String> =
        coordinates.filter { coordinate ->
            val group = coordinate.substringBefore(':')
            val artifact = coordinate.substringAfter(':', missingDelimiterValue = "")
            rules.any { rule -> group.startsWith(rule.groupPrefix) && artifact.startsWith(rule.artifactPrefix) }
        }
}
