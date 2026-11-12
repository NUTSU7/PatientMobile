package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AtomicVaultStoreConcurrencyTest {
    private class RealDispatchers : DispatcherProvider {
        override val default: CoroutineDispatcher = Dispatchers.Default
        override val io: CoroutineDispatcher = Dispatchers.IO
        override val main: CoroutineDispatcher = Dispatchers.Default
    }

    private class OverlapDetectingOps : AtomicFileOps {
        private val inFlight = AtomicInteger()
        val maxInFlight = AtomicInteger()
        val completedWrites = AtomicInteger()

        private fun <T> exclusive(action: () -> T): T {
            maxInFlight.accumulateAndGet(inFlight.incrementAndGet(), ::maxOf)
            try {
                Thread.sleep(STEP_PAUSE_MILLIS)
                return action()
            } finally {
                inFlight.decrementAndGet()
            }
        }

        override fun read(): FileRead = exclusive { FileRead.Absent }

        override fun writeTempSynced(bytes: ByteArray) = exclusive { true }

        override fun replaceWithTemp() = exclusive { true }

        override fun syncDirectory() = exclusive { completedWrites.incrementAndGet() > 0 }

        override fun deleteTemp() = exclusive { true }

        override fun overwriteWithZerosSynced() = exclusive { true }

        override fun delete() = exclusive { true }
    }

    @Test
    fun operations_runConcurrently_neverOverlapInFileOps() = runBlocking {
        val ops = OverlapDetectingOps()
        val store = AtomicVaultStore(ops, VaultCodec(), RealDispatchers())
        val golden = TestData.text("vault_v1_golden.json").encodeToByteArray()
        val vault = assertIs<VaultDecodeResult.Ok>(VaultCodec().decode(golden)).vault

        List(CONCURRENT_CALLS) { index ->
            async(Dispatchers.Default) {
                when (index % 3) {
                    0 -> store.write(vault)
                    1 -> store.read()
                    else -> store.destroy()
                }
            }
        }.awaitAll()

        assertEquals(1, ops.maxInFlight.get())
        assertEquals(WRITES, ops.completedWrites.get())
    }

    private companion object {
        const val CONCURRENT_CALLS = 12
        const val WRITES = 4
        const val STEP_PAUSE_MILLIS = 2L
    }
}
