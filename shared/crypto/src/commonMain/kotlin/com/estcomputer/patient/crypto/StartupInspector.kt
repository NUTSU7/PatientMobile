package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.StartupState

internal class StartupInspector(
    private val store: VaultStore,
    private val probe: AppDataProbe,
    private val keystore: KeystoreCipher,
    private val biometricKeystore: BiometricKeystore,
) {
    suspend fun inspect(): StartupState =
        if (probe.hasWipeMarker()) {
            StartupState.WipeInterrupted
        } else {
            when (store.read()) {
                is VaultReadResult.Ok, VaultReadResult.Corrupt, VaultReadResult.Unreadable ->
                    StartupState.VaultPresent
                VaultReadResult.Absent -> stateWithoutVault()
            }
        }

    private suspend fun stateWithoutVault(): StartupState =
        if (probe.hasAppData()) {
            StartupState.OrphanedData
        } else {
            // No data can exist without a vault, so keys left by an earlier install are stale.
            keystore.deleteAlias(CryptoConstants.HW_KEY_ALIAS)
            biometricKeystore.deleteAlias(CryptoConstants.BIO_KEY_ALIAS)
            StartupState.Clean
        }
}
