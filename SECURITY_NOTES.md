# Security notes

Accepted risks and manual checks for `shared:crypto`.

## Key material that cannot be zeroed

Tink copies a raw AES key into its own immutable objects when `TinkAeadCipher` builds a primitive. Those copies
stay until garbage collection. The app's own `SecretBytes` copies are zeroed as soon as they are used.

## Destroying `vault.json`

`AtomicVaultStore.destroy()` overwrites the file with zeros, forces the write to storage, then deletes it. Flash
wear levelling can leave older copies of the blocks on the device, so this is not the guarantee. The guarantee is
that the Keystore key is deleted first, which makes every wrapped key in the old file useless.

## Directory sync after a vault write

`AndroidAtomicFileOps.syncDirectory()` is best effort: a filesystem that refuses to open a directory is not
treated as a failed write. The rename itself is atomic; only its durability across a power cut is unproven there.
The device test runs it on an API 34 emulator, but the method never reports failure, so that run does not show
that the directory fsync itself succeeded. Real devices are unchecked.

## Unicode normalisation creates immutable copies

`java.text.Normalizer` takes and returns a `String`. Every NFKC pass over a password and every NFKD pass over
recovery words therefore leaves immutable copies on the heap until garbage collection. The `CharArray` inputs
and outputs are zeroed, but these copies cannot be. Accepted because no audited normaliser works on arrays.

## Keystore HW key without a secure lock screen

The HW key needs `setUnlockedDeviceRequired(true)`. Generating it on a device with no secure lock screen fails with
a `ProviderException`, which `AndroidKeystoreCipher.generateHwKey` does not catch after the StrongBox retry. Setup
must check `KeyguardManager.isDeviceSecure` first (Category Q). The device tests set a PIN on the emulator for this
reason: `adb shell locksettings set-pin 1234`.

## Keystore failures mapped to `HW_KEY_INVALIDATED`

`UnrecoverableKeyException` from `KeyStore.getKey` is treated like `KeyPermanentlyInvalidatedException`: the key
exists but can no longer be used, so the patient goes to the recovery path. The plan lists only the second one.

## Security level reporting

Security level is display only. The device tests ran on an API 34 emulator, so StrongBox and a hardware-backed TEE
are unchecked on real phones.
