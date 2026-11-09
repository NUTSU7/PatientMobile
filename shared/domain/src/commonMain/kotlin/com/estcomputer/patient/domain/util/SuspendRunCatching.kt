package com.estcomputer.patient.domain.util

import kotlin.coroutines.cancellation.CancellationException

// Lock-time cancellation must propagate, so unlike runCatching this never captures it.
public suspend inline fun <T> suspendRunCatching(block: () -> T): Result<T> =
    runCatching(block).onFailure { if (it is CancellationException) throw it }
