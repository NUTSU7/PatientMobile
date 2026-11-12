package com.estcomputer.patient.runtime

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** The one place that names the platform dispatchers; everything else receives a [DispatcherProvider]. */
internal class SystemDispatchers : DispatcherProvider {
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main.immediate
}
