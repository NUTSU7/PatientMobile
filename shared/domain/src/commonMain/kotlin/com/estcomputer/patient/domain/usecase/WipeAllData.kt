package com.estcomputer.patient.domain.usecase

import com.estcomputer.patient.domain.model.KeyDestructionResult
import com.estcomputer.patient.domain.model.WipeResult
import com.estcomputer.patient.domain.model.WipeStepResult
import com.estcomputer.patient.domain.port.KeyManager
import com.estcomputer.patient.domain.port.ReminderCanceller
import com.estcomputer.patient.domain.port.StorageWiper
import com.estcomputer.patient.domain.port.WipeMarker
import com.estcomputer.patient.domain.session.SessionCoordinator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Erases everything the app stores. Every step is safe to repeat, so a run after a crash at any point
 * ends in the same place. The marker is removed last; while it exists, start-up finishes the wipe.
 */
public class WipeAllData(
    private val sessionCoordinator: SessionCoordinator,
    private val keyManager: KeyManager,
    private val marker: WipeMarker,
    private val storage: StorageWiper,
    private val reminders: ReminderCanceller,
) {
    private val mutex = Mutex()

    public suspend operator fun invoke(): WipeResult =
        mutex.withLock {
            // A failed marker write does not stop the wipe: erasing now matters more than resuming later.
            marker.write()
            sessionCoordinator.lock()
            // ARH-09: keys go before files, so leftover ciphertext is useless.
            val keysDone = keyManager.destroyKeys() == KeyDestructionResult.DONE
            val storageDone = storage.deleteAll() == WipeStepResult.DONE
            reminders.cancelAll()
            if (keysDone && storageDone && marker.delete() == WipeStepResult.DONE) {
                sessionCoordinator.onWiped()
                WipeResult.DONE
            } else {
                WipeResult.INCOMPLETE
            }
        }
}
