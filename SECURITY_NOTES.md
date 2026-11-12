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

The HW key needs `setUnlockedDeviceRequired(true)`. Generating it on a device with no secure lock screen fails
with a `ProviderException`. `AndroidKeystoreCipher.generateHwKey` turns that into `DeviceNotSecure` when
`KeyguardManager.isDeviceSecure` is false, so a lock screen removed after the setup check is still handled. The
device tests need a PIN on the emulator: `adb shell locksettings set-pin 1234`. The no-lock-screen test runs only
without one (`adb shell locksettings clear --old 1234`).

## Keystore failures mapped to `HW_KEY_INVALIDATED`

`UnrecoverableKeyException` from `KeyStore.getKey` is treated like `KeyPermanentlyInvalidatedException`: the key
exists but can no longer be used, so the patient goes to the recovery path. Nothing may wipe data on
`HW_KEY_INVALIDATED`. `securityLevel()` reports such a key as `UNKNOWN` instead of throwing.

## Keystore checks still open

- Security level is display only. The device tests ran on an API 34 emulator only: StrongBox, a hardware-backed
  TEE, API 29 and 30 (the `isInsideSecureHardware` branch) and the bad-tag exception type on the older keystore
  are unchecked.
- Manual checklist, per test phone: lock the screen, then run an unlock. Expected: `DEVICE_LOCKED`, no failed
  attempt counted. On keystore2 (API 31+) a locked device may raise a different exception than
  `UserNotAuthenticatedException`; the host mapping test does not prove which one.

## Keystore BIO key

`AndroidBiometricKeystore` creates a key that needs a strong biometric for every use and is invalidated by any
biometric enrolment change. `generateBioKey` reports `NoBiometricEnrolled` when the platform refuses the key
because no secure lock screen or strong biometric exists. That maps every `InvalidAlgorithmParameterException`
from generation, so a wrong key spec would show up the same way; the device tests catch that on an enrolled
device. The AAD is added after authentication, because an authenticated key rejects every cipher call before it.

- The device tests skip themselves without an enrolled biometric. Enrol one on the emulator first. They check the
  key can be created and that `doFinal` without authentication fails. They were not run in the session that wrote
  them: no `adb` or device was available.
- Manual checklist, per test phone: enable biometrics, lock, unlock with the fingerprint; then enrol an extra
  fingerprint and expect the next biometric unlock to report an invalidated key and ask for the password.
- The biometric blob holds the password-unlock time as a wall-clock instant. A patient who sets the clock
  backwards is caught by the "unlock in the future" check; one who sets it forward within the same boot gains
  at most the clock shift on the 7-day limit. The boot count still forces the password after a reboot.
