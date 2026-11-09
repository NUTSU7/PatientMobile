package com.estcomputer.patient.domain.util

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest

class SuspendRunCatchingTest {

    @Test
    fun suspendRunCatching_onSuccess_returnsValue() = runTest {
        val result = suspendRunCatching { 42 }

        assertEquals(42, result.getOrNull())
    }

    @Test
    fun suspendRunCatching_onException_returnsFailure() = runTest {
        val result = suspendRunCatching { error("bug") }

        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }

    @Test
    fun suspendRunCatching_onCancellation_rethrows() = runTest {
        assertFailsWith<CancellationException> {
            suspendRunCatching { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun suspendRunCatching_nestedCancellation_rethrows() = runTest {
        val job = async(start = CoroutineStart.UNDISPATCHED) {
            suspendRunCatching {
                suspendRunCatching { awaitCancellation() }
            }
        }

        job.cancel()

        assertFailsWith<CancellationException> { job.await() }
    }
}
