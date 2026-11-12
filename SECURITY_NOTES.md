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
because no secure lock screen or strong biometric exists. An `InvalidAlgorithmParameterException` counts as
"none enrolled" only when `KeyguardManager.isDeviceSecure` and the platform `BiometricManager` agree; otherwise it
is rethrown, so a wrong key spec stays visible. The AAD is added after authentication, because an authenticated key rejects every cipher call before it.

- The device tests skip themselves without an enrolled biometric. They check the key can be created and that
  `doFinal` without authentication fails. They need `USE_BIOMETRIC`, which only the device-test manifest declares.
  On the API 34 emulator the other device tests passed and these skipped: no fingerprint was enrolled, so the
  authenticated path and keystore2's unauthenticated exception mapping have never run on a device.
- Manual checklist, per test phone: enable biometrics, lock, unlock with the fingerprint; then enrol an extra
  fingerprint and expect the next biometric unlock to report an invalidated key and ask for the password.
- The biometric blob holds the password-unlock time as a wall-clock instant. A patient who sets the clock
  backwards is caught by the "unlock in the future" check; one who sets it forward within the same boot gains
  at most the clock shift on the 7-day limit. The boot count still forces the password after a reboot.

## Vault key manager

- A biometric unlock never opens the one-minute window that lets the password change without the old one. Only
  a typed password, or the recovery words, do.
- The auto-wipe is requested after the key manager lock is released, because the wipe calls back into it. An
  in-memory `isWipePending` flag, set under the lock, makes every later attempt return `Wiped`, so a queued
  correct password cannot unlock a vault that is about to be wiped and the wipe is requested once. The wipe
  implementation (Category W) must write `wipe.pending` first and be idempotent.
- A process kill between a verified failure at the limit and the wipe skips that wipe. The counter is already at
  the limit, so the next wrong attempt wipes (the check is `>=`).
- Recovery replaces the hardware key before the vault write. If that write then fails, the password no longer
  opens the vault and the patient must use the recovery words again. The words still work, so nothing is lost.
- `destroyKeys` reports `VAULT_FILE_REMAINS` when the file cannot be erased. The keys are already gone, but the
  recovery words still open a leftover file, so the wipe must keep its marker and retry.
- Recovery attempts are limited in memory only. A restart resets the limit. The words carry 256 random bits.
- `PendingSetup.abandon()` zeroes the words at once but zeroes the key material only if no other key operation
  holds the lock; otherwise the next `lock()` does. Process death clears both.

## Session, wipe, auto-lock and device checks

- `SessionCoordinator.lock()` zeroes the keys even when a closer throws or the caller is cancelled. It also
  zeroes them when no session was announced, so a wiring mistake cannot leave keys in memory.
- `WipeAllData` still wipes when the `wipe.pending` marker cannot be written: erasing now matters more than
  being able to resume. If the process then dies mid-wipe, nothing restarts it, and the patient sees a
  locked app whose keys may already be gone.
- A wipe that cannot finish (`WipeResult.INCOMPLETE`) keeps the marker, and start-up runs it again.
  `VAULT_FILE_REMAINS` counts as incomplete for the reason given in the key manager section.
- Reminders: a wipe cancels shown notifications only. Scheduled alarms are cancelled when the reminders phase
  adds them; until then no alarm exists.
- `AutoLockPolicy` and `ScreenSecurityPolicy` are main-thread only and keep no locks. The wiring must call them
  from the main dispatcher.
- The screen-off receiver is registered dynamically when the process lifecycle is created. The system sends
  `ACTION_SCREEN_OFF` as a protected broadcast, so another app cannot trigger a lock with it.
- Before API 33 the recents screenshot is taken at pause, so `FLAG_SECURE` is added in `onPause` even when the
  patient allows screenshots. A manual check on API 29-32 and 33+ is still open.
- Device integrity checks are hints. The `su` and Magisk path lists are heuristics and can be bypassed. Package
  checks are skipped: they need a manifest `<queries>` entry, which the owner has not approved.
- Device tests on the API 34 emulator: the screen-off lock (via `input keyevent KEYCODE_SLEEP`), a full storage
  wipe in the real app directories, and the `FLAG_SECURE` flag on a window. Real devices are unchecked.

## App wiring and start-up

- `PatientRuntime` runs `RunStartup` in the application scope and `PatientAppHost` composes nothing until it ends.
  Order: finish an interrupted wipe, empty `tmp/` and `cacheDir/share/`, then publish the lock state. A wipe that
  stays incomplete, or data left without a vault (`OrphanedData`), keeps the app locked and publishes no vault
  state. The screens that explain it and offer an explicit wipe belong to the UI phase.
- Every `unlockedSessionScope` job runs on the default dispatcher, because the coordinator inherits the
  application scope's dispatcher.
- The key manager is built on first use and its two word lists are read on the IO dispatcher. The wipe and the key
  manager reach each other through `DeferredKeyManager` and a late-bound lookup, so neither is built first.
- User activity reaches the auto-lock through `Activity.onUserInteraction` (key presses and touch-downs) and
  `dispatchTouchEvent` (touch moves). `ComponentActivity.dispatchKeyEvent` is restricted API, so key releases and
  repeats are not forwarded.
- `AndroidBootInfo.bootCount()` fails closed when the platform setting cannot be read: it returns a random value
  fixed for the process and above any real boot count. Every process start then looks like a reboot, so a
  biometric unlock asks for the password and an active lockout re-arms. The setting exists on every supported API
  level, so this only covers a vendor quirk.
- The device integrity check is built and tested but not called yet: the warning needs a screen.
- `shared:crypto` exposes one public entry point, `VaultKeysFactory`, which returns only the `KeyManager` and
  `BiometricKeyManager` ports. **SECURITY-SENSITIVE**: it only assembles existing parts and changes no format,
  key, algorithm or AAD string.

## Release build

- The minified release build (R8) ran setup, lock, a wrong password, the right password and key destruction on an
  API 34 emulator, with no keep rule added. This used a throw-away harness that was not committed, because no
  setup screen exists yet. Tink, Bouncy Castle Argon2id, kotlinx-serialization and the Keystore all work after R8.
- `:androidApp:verifyReleaseManifest` passes. The merged release manifest has no `INTERNET`, only the allowed
  permissions and one exported component, the launcher activity. `androidx.startup.InitializationProvider` is not
  exported and keeps `ProcessLifecycleInitializer`, which the auto-lock needs.

## More accepted risks

- Compose text state: a text field keeps what the patient types in memory that the app cannot zero. The password
  field must clear its state right after derivation; the copies Compose made stay until garbage collection.
- A rooted device can restore an older `vault.json` and reset the failed-attempt counter, because the counter is
  stored in that file. The Keystore key stays, so this lets an attacker guess again but reveals nothing without
  the password. The recovery limit is in memory only for the same reason.
- Flash wear levelling can keep old blocks of a deleted file, as written for `vault.json` above. This applies to
  every file the wipe deletes. The keys are destroyed first, so the old blocks are ciphertext without a key.
- Security level per test phone: only an API 34 emulator was run. Nothing is recorded for a real phone, a
  StrongBox device or API 29 and 30 yet.
- The manual biometric checklist above is still open, as no fingerprint was enrolled on the emulator.

## Review follow-up decisions

Decided after the Phase 2 review, where doc 04 was silent.

- A cancelled password attempt that reached no verdict gives the attempt back: a cancellation while the counter
  is written or while Argon2 runs restores the previous vault. A process kill keeps the counted attempt, as
  before. Once the password check has a result, a cancellation changes nothing.
- A successful biometric unlock clears `failed_attempts` and the lockout. The strong biometric proves the
  patient, so earlier typos must not stay one step from the auto-wipe. Biometric unlock stays allowed during a
  password lockout; the 7-day and reboot rules still force the password.
- Changing the auto-wipe setting needs the password, checked as a counted attempt, or a password or recovery
  unlock less than one minute old. A biometric unlock alone cannot arm or disarm the wipe.
- The stored Argon2 memory must be between 32 and 64 MiB; a larger value is `VAULT_CORRUPT`, so an edited vault
  cannot exhaust the heap at every unlock.
- On lock, the session coordinator waits at most 3 seconds for session jobs to end. A job that ignores
  cancellation (a blocking call) can outlive the lock and keep a derived key until it returns; the closers make
  its next use of the data fail, and the master key is zeroed regardless.
- A process frozen in the background keeps the master key until it runs again. The lock is applied by the
  timer or on the return to the foreground, not while the process is frozen.
- After a wipe the app is not restarted: the lock state becomes `Unconfigured` and the UI shows the welcome
  screen. The keys are zeroed and the session is gone, so no decrypted state survives in memory. Revisit when
  the wipe confirmation screen is built.
- The wipe still deletes a fixed list of paths. Today `vault.json` is the only data file written. Phase 3 must
  keep the database in `noBackupFilesDir` under `AppPaths.DATABASE` and add an app-level device test that fails
  when any file outside the wipe list remains after setup, use and a wipe.

Still open, for the owner, because they change `vault.json` or doc 04:
- integrity of `export_public_key` and `wrapped_mk_recovery` (a MAC needs a new HKDF `info` string);
- splitting the attempt counters out of `vault.json` and keeping a backup copy of the key material;
- monotonic time instead of the wall clock in the biometric policy;
- a distinct result for an unknown newer vault `format`;
- rotating the hardware key on password change, and a flow to replace the recovery words.
