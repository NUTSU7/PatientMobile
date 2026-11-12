package com.estcomputer.patient.domain.session

/**
 * Releases something that holds decrypted data when the app locks, for example the database or an image
 * cache. Runs after the unlocked session scope is cancelled and before the keys are zeroed.
 * A failure in one closer never stops the others or the key zeroing.
 */
public fun interface SessionCloser {
    public suspend fun close()
}
