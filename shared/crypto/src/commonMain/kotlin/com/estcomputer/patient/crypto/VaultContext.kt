package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.port.BootInfo
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Clock

/** Cryptographic helpers shared by the vault flows. */
internal class KeyToolbox(
    val aead: AeadCipher,
    val keyDerivation: KeyDerivation,
    val exportKeyDeriver: ExportKeyDeriver,
    val bip39: Bip39Codec,
    val random: SecureRandomSource,
)

/** State and environment shared by the vault flows. [mutex] serialises every key operation. */
internal class VaultContext(
    val store: VaultStore,
    val deviceSecurity: DeviceSecurity,
    val bootInfo: BootInfo,
    val clock: Clock,
    val session: SessionKeys,
    val mutex: Mutex,
)
