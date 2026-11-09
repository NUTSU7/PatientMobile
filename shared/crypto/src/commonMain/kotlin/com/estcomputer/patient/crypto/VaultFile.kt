package com.estcomputer.patient.crypto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `vault.json`, format 1. Holds only wrapped keys and public data, never a secret in the clear.
 * Persisted contract: a field changes only with a format bump, a migration and a test.
 */
@Serializable
internal data class VaultFile(
    @SerialName("format") val format: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("kdf") val kdf: Kdf,
    @SerialName("hw_key_alias") val hwKeyAlias: String,
    @SerialName("wrapped_mk_password")
    @Serializable(with = Base64ByteArraySerializer::class)
    val wrappedMkPassword: ByteArray,
    @SerialName("wrapped_mk_recovery")
    @Serializable(with = Base64ByteArraySerializer::class)
    val wrappedMkRecovery: ByteArray,
    @SerialName("wrapped_mk_biometric")
    @Serializable(with = Base64ByteArraySerializer::class)
    val wrappedMkBiometric: ByteArray?,
    @SerialName("bio_key_alias") val bioKeyAlias: String?,
    @SerialName("export_public_key")
    @Serializable(with = Base64ByteArraySerializer::class)
    val exportPublicKey: ByteArray,
    @SerialName("failed_attempts") val failedAttempts: Int,
    @SerialName("wipe_after_failed_attempts") val wipeAfterFailedAttempts: Int,
    @SerialName("lockout_until_elapsed") val lockoutUntilElapsed: Long,
    @SerialName("lockout_boot_count") val lockoutBootCount: Int,
) {
    @Serializable
    data class Kdf(
        @SerialName("alg") val alg: String,
        @SerialName("salt") @Serializable(with = Base64ByteArraySerializer::class) val salt: ByteArray,
        @SerialName("memory_kib") val memoryKib: Int,
        @SerialName("iterations") val iterations: Int,
        @SerialName("parallelism") val parallelism: Int,
    ) {
        override fun toString(): String = "Kdf(alg=$alg)"
    }

    val kdfParams: KdfParams
        get() = KdfParams(kdf.salt, kdf.memoryKib, kdf.iterations, kdf.parallelism)

    override fun toString(): String = "VaultFile(format=$format)"
}
