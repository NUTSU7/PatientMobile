package com.estcomputer.patient.crypto

import android.app.KeyguardManager
import android.content.Context
import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import com.estcomputer.patient.domain.port.BiometricKeyManager
import com.estcomputer.patient.domain.port.BootInfo
import com.estcomputer.patient.domain.port.KeyManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.time.TimeSource

/** Both ports are backed by one object, so every key operation shares one mutex. */
public class VaultKeys internal constructor(
    public val keyManager: KeyManager,
    public val biometrics: BiometricKeyManager,
)

private class SystemDeviceSecurity(context: Context) : DeviceSecurity {
    private val keyguard = context.getSystemService(KeyguardManager::class.java)

    override fun isDeviceSecure(): Boolean = keyguard.isDeviceSecure
}

private class SystemSecureRandom : SecureRandomSource {
    private val random = SecureRandom()

    override fun nextBytes(size: Int): ByteArray = ByteArray(size).also(random::nextBytes)
}

private class LambdaAppDataProbe(
    private val markerCheck: suspend () -> Boolean,
    private val dataCheck: suspend () -> Boolean,
) : AppDataProbe {
    override suspend fun hasWipeMarker(): Boolean = markerCheck()

    override suspend fun hasAppData(): Boolean = dataCheck()
}

/** The Android pieces the key hierarchy needs. [vaultDirectory] must be `noBackupFilesDir`. */
public class VaultEnvironment(
    public val context: Context,
    public val vaultDirectory: File,
    public val dispatchers: DispatcherProvider,
    public val bootInfo: BootInfo,
    public val clock: Clock = Clock.System,
)

/**
 * What the key hierarchy asks of the rest of the app. [requestWipe] runs the full wipe: it is called after
 * the auto-wipe limit is reached and must write the `wipe.pending` marker first. It may reach back into the
 * returned key manager, so the caller breaks that cycle with a late-bound reference.
 */
public class VaultCallbacks(
    public val hasWipeMarker: suspend () -> Boolean,
    public val hasAppData: suspend () -> Boolean,
    public val requestWipe: suspend () -> Unit,
)

/**
 * The one place where the key hierarchy is assembled from its Android adapters. Everything else in this
 * module stays internal.
 */
public object VaultKeysFactory {

    /** The two word lists are read on the IO dispatcher. */
    public suspend fun create(environment: VaultEnvironment, callbacks: VaultCallbacks): VaultKeys {
        val (wordlist, commonPasswords) = withContext(environment.dispatchers.io) {
            BundledTextResource.lines(BIP39_WORDLIST) to BundledTextResource.lines(COMMON_PASSWORDS)
        }
        val probe = LambdaAppDataProbe(callbacks.hasWipeMarker, callbacks.hasAppData)
        val wipe = WipeRequester { callbacks.requestWipe() }
        val manager = assemble(environment, probe, wipe, Words(wordlist, commonPasswords))
        return VaultKeys(manager, manager)
    }

    private class Words(val bip39: List<String>, val commonPasswords: List<String>)

    private fun assemble(
        environment: VaultEnvironment,
        probe: AppDataProbe,
        wipe: WipeRequester,
        words: Words,
    ): VaultKeyManager {
        val dispatchers = environment.dispatchers
        val random = SystemSecureRandom()
        val normalizer = JavaUnicodeNormalizer()
        val keyDerivation = KeyDerivation(HkdfSha256())
        val keystore = AndroidKeystoreCipher(environment.context, dispatchers)
        val bioKeystore = AndroidBiometricKeystore(environment.context, dispatchers)
        val store = AtomicVaultStore(AndroidAtomicFileOps(environment.vaultDirectory), VaultCodec(), dispatchers)
        val context = VaultContext(
            store,
            SystemDeviceSecurity(environment.context),
            environment.bootInfo,
            environment.clock,
            SessionKeys(),
            Mutex(),
        )
        val kdf = Argon2idKeyDeriver(BouncyCastleArgon2id(), dispatchers, TimeSource.Monotonic)
        val tools = KeyToolbox(
            TinkAeadCipher(),
            keyDerivation,
            ExportKeyDeriver(keyDerivation, TinkX25519()),
            Bip39Codec(JcaSha256(), normalizer, words.bip39),
            random,
        )
        val validator = PasswordPolicyValidator(normalizer, PasswordBlocklist(words.commonPasswords))
        val inspector = StartupInspector(store, probe, keystore, bioKeystore)
        val hardware = HardwareWrap(keystore)
        val verifier = PasswordVerifier(keystore, kdf, tools, normalizer, context)
        val newPassword = NewPasswordDeriver(validator, kdf, random)
        val cleaner = BiometricCleaner(bioKeystore, context)
        val flows = VaultFlows(
            setup = SetupFlow(inspector, validator, kdf, hardware, tools, context),
            unlock = PasswordUnlockFlow(verifier, wipe, context),
            recovery = RecoveryFlow(newPassword, hardware, bioKeystore, tools, context),
            change = PasswordChangeFlow(verifier, newPassword, hardware, tools, wipe, context),
            biometrics = BiometricFlow(
                BiometricUnlockFlow(bioKeystore, BioPolicy(environment.bootInfo, environment.clock), cleaner, context),
                BiometricEnrolmentFlow(bioKeystore, cleaner, context),
            ),
            wipeSetting = WipeSettingFlow(verifier, wipe, context),
        )
        return VaultKeyManager(flows, inspector, keyDerivation, KeyDestroyer(hardware, bioKeystore, store), context)
    }

    private const val BIP39_WORDLIST = "bip39_english.txt"
    private const val COMMON_PASSWORDS = "common_passwords.txt"
}
