package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.SecretBytes

/** The only places an HKDF `info` string meets a key. Each result is a fresh copy the caller zeroes. */
internal class KeyDerivation(private val hkdf: Hkdf) {
    fun deriveDbk(masterKey: SecretBytes): SecretBytes = hkdf.derive(masterKey, CryptoConstants.HKDF_INFO_DB)

    fun deriveFkek(masterKey: SecretBytes): SecretBytes = hkdf.derive(masterKey, CryptoConstants.HKDF_INFO_FILE_KEK)

    fun deriveRwk(recoverySecret: SecretBytes): SecretBytes =
        hkdf.derive(recoverySecret, CryptoConstants.HKDF_INFO_RECOVERY_WRAP)

    fun deriveExportPrivate(recoverySecret: SecretBytes): SecretBytes =
        hkdf.derive(recoverySecret, CryptoConstants.HKDF_INFO_EXPORT_KEM)
}
