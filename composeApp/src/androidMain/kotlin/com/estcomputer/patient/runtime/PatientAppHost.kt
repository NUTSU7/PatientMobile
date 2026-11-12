package com.estcomputer.patient.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Composes [content] only after start-up has finished, so nothing is drawn while a wipe may still be running. */
@Composable
public fun PatientAppHost(runtime: PatientRuntime, content: @Composable () -> Unit) {
    val isReady by runtime.isStartupComplete.collectAsStateWithLifecycle()
    if (isReady) content()
}
