package com.estcomputer.patient.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DomainStatesTest {

    @Test
    fun lockState_whenExhaustive_compilesWithoutElse() {
        val states = listOf(LockState.Unconfigured, LockState.Locked(isRecoveryRequired = true), LockState.Unlocked)

        val labels = states.map { state ->
            when (state) {
                LockState.Unconfigured -> "unconfigured"
                is LockState.Locked -> "locked"
                LockState.Unlocked -> "unlocked"
            }
        }

        assertEquals(listOf("unconfigured", "locked", "unlocked"), labels)
    }

    @Test
    fun startupState_whenExhaustive_compilesWithoutElse() {
        val labels = StartupState.entries.map { state ->
            when (state) {
                StartupState.Clean -> "clean"
                StartupState.VaultPresent -> "vault"
                StartupState.WipeInterrupted -> "wipe"
                StartupState.OrphanedData -> "orphaned"
            }
        }

        assertEquals(4, labels.size)
    }
}
