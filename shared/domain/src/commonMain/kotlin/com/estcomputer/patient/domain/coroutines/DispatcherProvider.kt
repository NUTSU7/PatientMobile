package com.estcomputer.patient.domain.coroutines

import kotlinx.coroutines.CoroutineDispatcher

public interface DispatcherProvider {
    public val default: CoroutineDispatcher
    public val io: CoroutineDispatcher
    public val main: CoroutineDispatcher
}
