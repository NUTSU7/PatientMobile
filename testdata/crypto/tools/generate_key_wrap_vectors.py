#!/usr/bin/env python3
"""Writes key_wrap_vectors.json: the two persisted wrap layers as ciphertexts the app must open.

An AES-256-GCM written from FIPS 197 and NIST SP 800-38D with the Python standard library only, independent of
the app code and of Tink. Before it writes anything it must reproduce every AES-256-GCM case of
aes_gcm_wycheproof.json (96-bit IV, 128-bit tag) and the HKDF output stored in hkdf_vectors.json.

Layout of a wrapped key: nonce (12 bytes) || ciphertext || tag (16 bytes).
Layers:
  password: key is the Argon2id tag of argon2id_app_vectors.json, AAD "patient/mk/password/v1".
  recovery: key is HKDF-SHA256(recovery secret, info "patient/recovery-wrap/v1"), AAD "patient/mk/recovery/v1".
The master key, recovery secret and nonces are fixed byte patterns.

Usage: python3 generate_key_wrap_vectors.py <testdata/crypto directory>
"""
import hashlib
import hmac
import json
import sys
from pathlib import Path


def gf_mul_byte(a: int, b: int) -> int:
    result = 0
    for _ in range(8):
        if b & 1:
            result ^= a
        carry = a & 0x80
        a = (a << 1) & 0xFF
        if carry:
            a ^= 0x1B
        b >>= 1
    return result


def build_sbox() -> list:
    inverse = [0] * 256
    for value in range(1, 256):
        for candidate in range(1, 256):
            if gf_mul_byte(value, candidate) == 1:
                inverse[value] = candidate
                break
    sbox = []
    for value in range(256):
        x = inverse[value]
        y = x
        for shift in (1, 2, 3, 4):
            y ^= ((x << shift) | (x >> (8 - shift))) & 0xFF
        sbox.append(y ^ 0x63)
    return sbox


SBOX = build_sbox()
assert SBOX[0x00] == 0x63 and SBOX[0x53] == 0xED and SBOX[0xFF] == 0x16


def expand_key(key: bytes) -> list:
    assert len(key) == 32
    words = [list(key[i:i + 4]) for i in range(0, 32, 4)]
    rcon = 1
    for index in range(8, 60):
        word = list(words[index - 1])
        if index % 8 == 0:
            word = word[1:] + word[:1]
            word = [SBOX[b] for b in word]
            word[0] ^= rcon
            rcon = gf_mul_byte(rcon, 2)
        elif index % 8 == 4:
            word = [SBOX[b] for b in word]
        words.append([a ^ b for a, b in zip(words[index - 8], word)])
    return [sum(words[4 * r:4 * r + 4], []) for r in range(15)]


def encrypt_block(round_keys: list, block: bytes) -> bytes:
    state = [b ^ k for b, k in zip(block, round_keys[0])]
    for round_number in range(1, 15):
        state = [SBOX[b] for b in state]
        state = [state[(column * 4 + row + row * 4) % 16] for column in range(4) for row in range(4)]
        if round_number != 14:
            mixed = []
            for column in range(4):
                a = state[column * 4:column * 4 + 4]
                mixed += [
                    gf_mul_byte(a[0], 2) ^ gf_mul_byte(a[1], 3) ^ a[2] ^ a[3],
                    a[0] ^ gf_mul_byte(a[1], 2) ^ gf_mul_byte(a[2], 3) ^ a[3],
                    a[0] ^ a[1] ^ gf_mul_byte(a[2], 2) ^ gf_mul_byte(a[3], 3),
                    gf_mul_byte(a[0], 3) ^ a[1] ^ a[2] ^ gf_mul_byte(a[3], 2),
                ]
            state = mixed
        state = [b ^ k for b, k in zip(state, round_keys[round_number])]
    return bytes(state)


FIPS197_KEY = bytes(range(32))
FIPS197_PLAIN = bytes.fromhex("00112233445566778899aabbccddeeff")
FIPS197_CIPHER = "8ea2b7ca516745bfeafc49904b496089"
assert encrypt_block(expand_key(FIPS197_KEY), FIPS197_PLAIN).hex() == FIPS197_CIPHER


def gf128_mul(x: int, y: int) -> int:
    reduction = 0xE1 << 120
    z = 0
    for bit in range(128):
        if (x >> (127 - bit)) & 1:
            z ^= y
        y = (y >> 1) ^ reduction if y & 1 else y >> 1
    return z


def ghash(h: int, aad: bytes, ciphertext: bytes) -> int:
    def pad(data: bytes) -> bytes:
        return data + bytes(-len(data) % 16)

    data = pad(aad) + pad(ciphertext) + (len(aad) * 8).to_bytes(8, "big") + (len(ciphertext) * 8).to_bytes(8, "big")
    y = 0
    for offset in range(0, len(data), 16):
        y = gf128_mul(y ^ int.from_bytes(data[offset:offset + 16], "big"), h)
    return y


def gcm_seal(key: bytes, nonce: bytes, aad: bytes, plaintext: bytes) -> bytes:
    assert len(nonce) == 12
    round_keys = expand_key(key)
    h = int.from_bytes(encrypt_block(round_keys, bytes(16)), "big")
    counter0 = nonce + (1).to_bytes(4, "big")
    ciphertext = b""
    for index in range(0, len(plaintext), 16):
        counter = nonce + (2 + index // 16).to_bytes(4, "big")
        stream = encrypt_block(round_keys, counter)
        ciphertext += bytes(a ^ b for a, b in zip(plaintext[index:index + 16], stream))
    mask = int.from_bytes(encrypt_block(round_keys, counter0), "big")
    tag = (ghash(h, aad, ciphertext) ^ mask).to_bytes(16, "big")
    return ciphertext + tag


def hkdf(ikm: bytes, info: bytes, length: int = 32) -> bytes:
    prk = hmac.new(bytes(32), ikm, hashlib.sha256).digest()
    okm, block, counter = b"", b"", 1
    while len(okm) < length:
        block = hmac.new(prk, block + info + bytes([counter]), hashlib.sha256).digest()
        okm += block
        counter += 1
    return okm[:length]


def check_against_wycheproof(directory: Path) -> int:
    checked = 0
    for case in json.loads((directory / "aes_gcm_wycheproof.json").read_text()):
        if case["result"] != "valid" or len(bytes.fromhex(case["key"])) != 32:
            continue
        sealed = gcm_seal(
            bytes.fromhex(case["key"]), bytes.fromhex(case["iv"]), bytes.fromhex(case["aad"]), bytes.fromhex(case["msg"])
        )
        assert sealed.hex() == case["ct"] + case["tag"], case["tcId"]
        checked += 1
    assert checked > 0
    return checked


def main(directory: Path) -> None:
    checked = check_against_wycheproof(directory)

    stored_hkdf = {c["name"]: c for c in json.loads((directory / "hkdf_vectors.json").read_text())}
    recovery_secret = bytes(range(32))
    recovery_info = "patient/recovery-wrap/v1"
    rwk = hkdf(recovery_secret, recovery_info.encode("utf-8"))
    assert rwk.hex() == stored_hkdf["app:" + recovery_info]["okm"]

    argon = json.loads((directory / "argon2id_app_vectors.json").read_text())[0]
    password_key = bytes.fromhex(argon["tag"])

    master_key = bytes(0xA0 + i for i in range(32))
    layers = [
        ("password", password_key, bytes(range(0x10, 0x1C)), "patient/mk/password/v1"),
        ("recovery", rwk, bytes(range(0x20, 0x2C)), "patient/mk/recovery/v1"),
    ]
    vectors = []
    for name, key, nonce, aad in layers:
        blob = nonce + gcm_seal(key, nonce, aad.encode("utf-8"), master_key)
        vectors.append({
            "name": name,
            "key": key.hex(),
            "aad": aad.encode("utf-8").hex(),
            "master_key": master_key.hex(),
            "blob": blob.hex(),
        })
    document = {
        "recovery_secret": recovery_secret.hex(),
        "recovery_info": recovery_info.encode("utf-8").hex(),
        "layers": vectors,
    }
    (directory / "key_wrap_vectors.json").write_text(json.dumps(document, indent=2) + "\n")
    print(f"verified against {checked} Wycheproof cases; wrote key_wrap_vectors.json")


if __name__ == "__main__":
    main(Path(sys.argv[1]))
