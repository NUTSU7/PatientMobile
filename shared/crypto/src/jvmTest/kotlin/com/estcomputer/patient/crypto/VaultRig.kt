package com.estcomputer.patient.crypto

import com.estcomputer.patient.domain.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

private class UnconfinedDispatchers(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}

/** All fakes plus the real codec, store, validator and BIP-39 codec, wired the way production is. */
internal class VaultRig(scope: TestScope) {
    val random = FakeRandom()
    val files = MemoryFileOps()
    val boot = FakeBootInfo()
    val clock = FakeClock()
    val security = FakeDeviceSecurity()
    val probe = FakeProbe()
    val kdf = FakeKdf()
    val aead = FakeAead(random)
    val keystore = FakeKeystore(random)
    val bioKeystore = FakeBiometricKeystore(random)
    val wipe = FakeWipeRequester()
    val session = SessionKeys()
    val mutex = Mutex()
    private val dispatchers = UnconfinedDispatchers(UnconfinedTestDispatcher(scope.testScheduler))
    val store = AtomicVaultStore(files, VaultCodec(), dispatchers)
    val normalizer = JavaUnicodeNormalizer()
    val keyDerivation = KeyDerivation(FakeHkdf())
    val bip39 = Bip39Codec(
        JcaSha256(),
        normalizer,
        TestData.text("bip39_english.txt").lines().filter { it.isNotEmpty() },
    )
    val validator = PasswordPolicyValidator(normalizer, PasswordBlocklist(listOf("parola123", "password")))
    val inspector = StartupInspector(store, probe, keystore, bioKeystore)

    private val context = VaultContext(store, security, boot, clock, session, mutex)
    private val tools = KeyToolbox(
        aead, keyDerivation, ExportKeyDeriver(keyDerivation, FakeX25519()), bip39, random,
    )

    val setup = SetupFlow(inspector, validator, kdf, keystore, tools, context)
}
