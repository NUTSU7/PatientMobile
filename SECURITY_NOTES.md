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
Not yet exercised on a device.

## Unicode normalisation creates immutable copies

`java.text.Normalizer` takes and returns a `String`. Every NFKC pass over a password and every NFKD pass over
recovery words therefore leaves immutable copies on the heap until garbage collection. The `CharArray` inputs
and outputs are zeroed, but these copies cannot be. Accepted because no audited normaliser works on arrays.
