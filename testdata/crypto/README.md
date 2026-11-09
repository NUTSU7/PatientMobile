# Crypto test data

Fixed inputs for known-answer tests. Never regenerate a file to make a test pass.

| File | Source | Licence | SHA-256 |
|---|---|---|---|
| `bip39_english.txt` | https://github.com/bitcoin/bips/blob/master/bip-0039/english.txt | BIP-39 reference list (MIT, bitcoin/bips) | `2f5eed53a4727b4bf8880d8f3f199efc90e58503646d9ff8eff3a2ed3b24dbda` |
| `bip39_vectors.json` | 256-bit entropy/mnemonic pairs of https://github.com/trezor/python-mnemonic/blob/master/vectors.json, extracted with `tools/extract_bip39_vectors.py`. The raw file had SHA-256 `fa3b937b7cff9c9b8ecd3aa011faeb8d6dd67993174b72326e83f4de8fdb30f8`. | MIT | `f8c90de602baf634e86c4a4aa682e3890a881a9c0c82b8c32d859b8de303460b` |
| `common_passwords_seclists_10k.txt` | https://github.com/danielmiessler/SecLists/blob/master/Passwords/Common-Credentials/10k-most-common.txt | MIT | `68782d6a4a19a4768d5f15dd66bd534e7a33055cc755411e33f16d18c50fdcce` |

Downloaded 2026-11-09.
