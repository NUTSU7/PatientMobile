package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.model.KeyError
import com.estcomputer.patient.domain.model.SecretBytes
import com.estcomputer.patient.domain.model.SecretChars
import com.estcomputer.patient.domain.port.BiometricChallenge
import com.estcomputer.patient.domain.port.BootInfo
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** Keyed toy cipher with the real wire layout: `nonce (12) ‖ body ‖ tag (16)`. Rejects a wrong key or aad. */
internal object ToyCipher {
    fun seal(key: ByteArray, nonce: ByteArray, plaintext: ByteArray, aad: String): ByteArray {
        val body = ByteArray(plaintext.size) { (plaintext[it].toInt() xor key[it % key.size].toInt()).toByte() }
        return nonce + body + tag(key, nonce, body, aad)
    }

    /** Null when the tag does not match. */
    fun open(key: ByteArray, blob: ByteArray, aad: String): ByteArray? {
        val minimum = CryptoConstants.GCM_NONCE_BYTES + CryptoConstants.GCM_TAG_BYTES
        val nonce = blob.copyOfRange(0, minOf(CryptoConstants.GCM_NONCE_BYTES, blob.size))
        val bodyEnd = maxOf(CryptoConstants.GCM_NONCE_BYTES, blob.size - CryptoConstants.GCM_TAG_BYTES)
        val body = if (blob.size < minimum) ByteArray(0) else blob.copyOfRange(nonce.size, bodyEnd)
        val tag = if (blob.size < minimum) ByteArray(0) else blob.copyOfRange(bodyEnd, blob.size)
        val isValid = blob.size >= minimum && tag.contentEquals(tag(key, nonce, body, aad))
        if (!isValid) return null
        return ByteArray(body.size) { (body[it].toInt() xor key[it % key.size].toInt()).toByte() }
    }

    private fun tag(key: ByteArray, nonce: ByteArray, body: ByteArray, aad: String): ByteArray {
        var h = 17
        for (b in key + nonce + body + aad.encodeToByteArray()) h = h * 31 + b
        return ByteArray(CryptoConstants.GCM_TAG_BYTES) { (h + it * 7919).toByte() }
    }
}

internal class FakeRandom : SecureRandomSource {
    private var counter = 1

    override fun nextBytes(size: Int): ByteArray = ByteArray(size) { (counter++ * 13 + it).toByte() }
}

internal class FakeAead(private val random: SecureRandomSource) : AeadCipher {
    override fun seal(key: SecretBytes, plaintext: SecretBytes, aad: String): ByteArray =
        key.read { k ->
            plaintext.read { p ->
                ToyCipher.seal(k, random.nextBytes(CryptoConstants.GCM_NONCE_BYTES), p, aad)
            }
        }

    override fun open(key: SecretBytes, ciphertext: ByteArray, aad: String): OpenResult {
        val plain = key.read { ToyCipher.open(it, ciphertext, aad) }
        return if (plain == null) OpenResult.AuthFailed else OpenResult.Ok(SecretBytes.wrap(plain))
    }
}

internal class FakeHkdf : Hkdf {
    override fun derive(ikm: SecretBytes, info: String): SecretBytes {
        val infoBytes = info.encodeToByteArray()
        return ikm.read { k ->
            SecretBytes.wrap(
                ByteArray(CryptoConstants.KEY_SIZE_BYTES) {
                    (k[it].toInt() xor infoBytes[it % infoBytes.size].toInt() xor (it * 3)).toByte()
                },
            )
        }
    }
}

internal class FakeX25519 : X25519 {
    override fun publicKey(privateKey: SecretBytes): ByteArray =
        privateKey.read { k -> ByteArray(k.size) { (k[it].toInt() + 1).toByte() } }
}

/** Stands in for Argon2id: a key from the password and salt, the real bounds check, and failure injection. */
internal class FakeKdf : PasswordKdf, KdfCalibrator {
    var deriveCalls = 0
    var calibrateCalls = 0
    var failure: KeyError? = null

    /** Simulates the process dying while the KDF runs. */
    var crash: Throwable? = null
    var onDerive: suspend () -> Unit = {}

    private fun key(password: SecretChars, salt: ByteArray): SecretBytes {
        val bytes = password.read { chars ->
            ByteArray(CryptoConstants.KEY_SIZE_BYTES) { i ->
                val c = if (chars.isEmpty()) 0 else chars[i % chars.size].code
                (c + salt[i % salt.size] + i).toByte()
            }
        }
        return SecretBytes.wrap(bytes)
    }

    override suspend fun derive(password: SecretChars, params: KdfParams): KdfResult {
        deriveCalls++
        onDerive()
        crash?.let { throw it }
        val error = failure
        return when {
            !params.isWithinBounds -> KdfResult.Failed(KeyError.VAULT_CORRUPT)
            error != null -> KdfResult.Failed(error)
            else -> KdfResult.Derived(key(password, params.salt))
        }
    }

    override suspend fun calibrate(password: SecretChars, salt: ByteArray): CalibrationResult {
        calibrateCalls++
        val error = failure
        val params = KdfParams(
            salt,
            CryptoConstants.ARGON2_MEMORY_KIB,
            CryptoConstants.ARGON2_ITERATIONS,
            CryptoConstants.ARGON2_PARALLELISM,
        )
        return if (error != null) {
            CalibrationResult.Failed(error)
        } else {
            CalibrationResult.Calibrated(params, key(password, salt))
        }
    }
}

internal class FakeKeystore(private val random: SecureRandomSource) : KeystoreCipher {
    val keys = mutableMapOf<String, ByteArray>()
    var openError: KeystoreError? = null
    var sealError: KeystoreError? = null

    /** Fails the next seal only. */
    var failNextSeal: KeystoreError? = null
    var isDeviceSecure = true
    var generateCalls = 0
    val deleted = mutableListOf<String>()

    override suspend fun generateHwKey(alias: String): KeystoreGenerateResult {
        generateCalls++
        if (!isDeviceSecure) return KeystoreGenerateResult.DeviceNotSecure
        keys[alias] = random.nextBytes(CryptoConstants.KEY_SIZE_BYTES)
        return KeystoreGenerateResult.Ok(SecurityLevel.TRUSTED_ENVIRONMENT)
    }

    override suspend fun seal(alias: String, plaintext: SecretBytes, aad: String): KeystoreSealResult {
        val key = keys[alias]
        val once = failNextSeal
        failNextSeal = null
        val error = once ?: sealError ?: if (key == null) KeystoreError.HW_KEY_MISSING else null
        return if (error != null || key == null) {
            KeystoreSealResult.Failed(error ?: KeystoreError.HW_KEY_MISSING)
        } else {
            KeystoreSealResult.Ok(
                plaintext.read { ToyCipher.seal(key, random.nextBytes(CryptoConstants.GCM_NONCE_BYTES), it, aad) },
            )
        }
    }

    override suspend fun open(alias: String, ciphertext: ByteArray, aad: String): KeystoreOpenResult {
        val key = keys[alias]
        val error = openError ?: if (key == null) KeystoreError.HW_KEY_MISSING else null
        if (error != null || key == null) return KeystoreOpenResult.Failed(error ?: KeystoreError.HW_KEY_MISSING)
        val plain = ToyCipher.open(key, ciphertext, aad)
        return if (plain == null) {
            KeystoreOpenResult.Failed(KeystoreError.RECOVERY_REQUIRED)
        } else {
            KeystoreOpenResult.Ok(SecretBytes.wrap(plain))
        }
    }

    override suspend fun securityLevel(alias: String): SecurityLevel? =
        if (alias in keys) SecurityLevel.TRUSTED_ENVIRONMENT else null

    override suspend fun deleteAlias(alias: String) {
        deleted.add(alias)
        keys.remove(alias)
    }
}

internal class FakeChallenge(val isEncrypt: Boolean, val key: ByteArray, val nonce: ByteArray) : BiometricChallenge

internal class FakeBiometricKeystore(private val random: SecureRandomSource) : BiometricKeystore {
    val keys = mutableMapOf<String, ByteArray>()
    var isEnrolled = true
    var beginError: BiometricKeystoreError? = null
    var sealError: BiometricKeystoreError? = null
    var openError: BiometricKeystoreError? = null
    val deleted = mutableListOf<String>()

    override suspend fun generateBioKey(alias: String): BiometricKeystoreGenerateResult {
        if (!isEnrolled) return BiometricKeystoreGenerateResult.NoBiometricEnrolled
        keys[alias] = random.nextBytes(CryptoConstants.KEY_SIZE_BYTES)
        return BiometricKeystoreGenerateResult.Ok(SecurityLevel.TRUSTED_ENVIRONMENT)
    }

    override suspend fun beginSeal(alias: String): BiometricKeystoreBeginResult =
        begin(alias, true, random.nextBytes(CryptoConstants.GCM_NONCE_BYTES))

    override suspend fun beginOpen(alias: String, blob: ByteArray): BiometricKeystoreBeginResult =
        begin(alias, false, blob.copyOfRange(0, CryptoConstants.GCM_NONCE_BYTES))

    private fun begin(alias: String, isEncrypt: Boolean, nonce: ByteArray): BiometricKeystoreBeginResult {
        val key = keys[alias]
        val error = beginError ?: if (key == null) BiometricKeystoreError.KEY_MISSING else null
        return if (error != null || key == null) {
            BiometricKeystoreBeginResult.Failed(error ?: BiometricKeystoreError.KEY_MISSING)
        } else {
            BiometricKeystoreBeginResult.Ready(FakeChallenge(isEncrypt, key, nonce))
        }
    }

    override suspend fun seal(
        challenge: BiometricChallenge,
        plaintext: SecretBytes,
        aad: String,
    ): BiometricKeystoreSealResult {
        val own = challenge as FakeChallenge
        val error = sealError
        return if (error != null) {
            BiometricKeystoreSealResult.Failed(error)
        } else {
            BiometricKeystoreSealResult.Ok(plaintext.read { ToyCipher.seal(own.key, own.nonce, it, aad) })
        }
    }

    override suspend fun open(
        challenge: BiometricChallenge,
        blob: ByteArray,
        aad: String,
    ): BiometricKeystoreOpenResult {
        val own = challenge as FakeChallenge
        val error = openError
        val plain = if (error == null) ToyCipher.open(own.key, blob, aad) else null
        return when {
            error != null -> BiometricKeystoreOpenResult.Failed(error)
            plain == null -> BiometricKeystoreOpenResult.Failed(BiometricKeystoreError.BLOB_REJECTED)
            else -> BiometricKeystoreOpenResult.Ok(SecretBytes.wrap(plain))
        }
    }

    override suspend fun deleteAlias(alias: String) {
        deleted.add(alias)
        keys.remove(alias)
    }
}

internal class FakeBootInfo(var bootCount: Int = 1, var elapsedMillis: Long = 1_000_000) : BootInfo {
    override fun bootCount(): Int = bootCount

    override fun elapsedRealtimeMillis(): Long = elapsedMillis
}

internal class FakeClock(var current: Instant = Instant.parse("2026-11-12T10:00:00Z")) : Clock {
    override fun now(): Instant = current

    fun advance(duration: Duration) {
        current += duration
    }
}

internal class FakeDeviceSecurity(var isSecure: Boolean = true) : DeviceSecurity {
    override fun isDeviceSecure(): Boolean = isSecure
}

internal class FakeProbe : AppDataProbe {
    var hasMarker = false
    var hasData = false

    override suspend fun hasWipeMarker(): Boolean = hasMarker

    override suspend fun hasAppData(): Boolean = hasData
}

/** Records the wipe request and, like the real wipe, reaches back into the key manager while it runs. */
internal class FakeWipeRequester : WipeRequester {
    var calls = 0
    var onWipe: suspend () -> Unit = {}

    override suspend fun wipeAllData() {
        calls++
        onWipe()
    }
}

internal class MemoryFileOps : AtomicFileOps {
    var vaultBytes: ByteArray? = null
    var tempBytes: ByteArray? = null
    var failingStep: String? = null
    var writes = 0

    private fun step(name: String, action: () -> Unit): Boolean {
        if (failingStep == name) return false
        action()
        return true
    }

    override fun read(): FileRead =
        when {
            failingStep == "read" -> FileRead.Failed
            else -> vaultBytes?.let { FileRead.Content(it.copyOf()) } ?: FileRead.Absent
        }

    override fun writeTempSynced(bytes: ByteArray) = step("temp") { tempBytes = bytes.copyOf() }

    override fun replaceWithTemp() = step("replace") {
        vaultBytes = tempBytes
        tempBytes = null
        writes++
    }

    override fun syncDirectory() = step("sync") {}

    override fun deleteTemp() = step("deleteTemp") { tempBytes = null }

    override fun overwriteWithZerosSynced() = step("zero") { vaultBytes = vaultBytes?.let { ByteArray(it.size) } }

    override fun delete() = step("delete") { vaultBytes = null }
}
