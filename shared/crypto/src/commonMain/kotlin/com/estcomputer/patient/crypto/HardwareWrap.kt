package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError

internal sealed interface HardwareWrapResult {
    class Ok(val blob: ByteArray) : HardwareWrapResult {
        override fun toString(): String = "Ok(***)"
    }

    data class Failed(val error: KeyError) : HardwareWrapResult
}

/** The outer, device-bound layer around the password-wrapped master key. */
internal class HardwareWrap(private val keystore: KeystoreCipher) {
    suspend fun create(alias: String): KeystoreGenerateResult = keystore.generateHwKey(alias)

    suspend fun drop(alias: String) = keystore.deleteAlias(alias)

    /** Seals under the existing key and never creates one. */
    suspend fun wrap(alias: String, inner: ByteArray): HardwareWrapResult =
        when (val sealed = keystore.sealBytes(alias, inner, CryptoConstants.AAD_MK_HW)) {
            is KeystoreSealResult.Ok -> HardwareWrapResult.Ok(sealed.blob)
            is KeystoreSealResult.Failed -> HardwareWrapResult.Failed(sealed.error.toKeyError())
        }

    /**
     * Like [wrap], but a missing or invalidated key is replaced. Only recovery may do this: the old key is
     * what made the previous wrapping unrecoverable.
     */
    suspend fun wrapRebuildingKey(alias: String, inner: ByteArray): HardwareWrapResult {
        val first = wrap(alias, inner)
        val isLocked = first is HardwareWrapResult.Failed && first.error == KeyError.DEVICE_LOCKED
        return if (first is HardwareWrapResult.Ok || isLocked) first else rebuild(alias, inner)
    }

    private suspend fun rebuild(alias: String, inner: ByteArray): HardwareWrapResult {
        keystore.deleteAlias(alias)
        return when (keystore.generateHwKey(alias)) {
            KeystoreGenerateResult.DeviceNotSecure -> HardwareWrapResult.Failed(KeyError.DEVICE_NOT_SECURE)
            is KeystoreGenerateResult.Ok -> wrap(alias, inner)
        }
    }
}
