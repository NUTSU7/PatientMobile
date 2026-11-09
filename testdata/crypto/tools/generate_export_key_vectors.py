"""Independent export public key reference built on the Python standard library only.

Usage: python3 generate_export_key_vectors.py <output export_key_vectors.json>
Implements X25519 (RFC 7748 section 5) and HKDF-SHA256 (RFC 5869). It first reproduces the RFC 7748 section 6.1
Alice and Bob public keys and refuses to write anything if either differs. The app case derives the export
private key with the persisted info string from a fixed recovery secret. Never feed this script from app code.
"""
import hashlib
import hmac
import json
import sys

P = 2**255 - 19
A24 = 121665
HASH_LEN = 32
EXPORT_KEM_INFO = "patient/export-kem/v1"

RFC_ALICE_PRIVATE = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a"
RFC_ALICE_PUBLIC = "8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a"
RFC_BOB_PRIVATE = "5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb"
RFC_BOB_PUBLIC = "de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f"


def clamp(k: bytes) -> int:
    b = bytearray(k)
    b[0] &= 248
    b[31] &= 127
    b[31] |= 64
    return int.from_bytes(b, "little")


def x25519(k: bytes, u: bytes) -> bytes:
    scalar = clamp(k)
    x1 = int.from_bytes(u, "little") & ((1 << 255) - 1)
    x2, z2, x3, z3, swap = 1, 0, x1, 1, 0
    for t in reversed(range(255)):
        bit = (scalar >> t) & 1
        swap ^= bit
        if swap:
            x2, x3, z2, z3 = x3, x2, z3, z2
        swap = bit
        a = (x2 + z2) % P
        aa = a * a % P
        b = (x2 - z2) % P
        bb = b * b % P
        e = (aa - bb) % P
        c = (x3 + z3) % P
        d = (x3 - z3) % P
        da = d * a % P
        cb = c * b % P
        x3 = pow(da + cb, 2, P)
        z3 = x1 * pow(da - cb, 2, P) % P
        x2 = aa * bb % P
        z2 = e * (aa + A24 * e) % P
    if swap:
        x2, x3, z2, z3 = x3, x2, z3, z2
    return (x2 * pow(z2, P - 2, P) % P).to_bytes(32, "little")


def hkdf(ikm: bytes, info: bytes, length: int) -> bytes:
    prk = hmac.new(bytes(HASH_LEN), ikm, hashlib.sha256).digest()
    okm, block, counter = b"", b"", 1
    while len(okm) < length:
        block = hmac.new(prk, block + info + bytes([counter]), hashlib.sha256).digest()
        okm += block
        counter += 1
    return okm[:length]


BASE = bytes([9]) + bytes(31)
for private, public in ((RFC_ALICE_PRIVATE, RFC_ALICE_PUBLIC), (RFC_BOB_PRIVATE, RFC_BOB_PUBLIC)):
    assert x25519(bytes.fromhex(private), BASE).hex() == public, "RFC 7748 section 6.1 mismatch"

recovery_secret = bytes(range(32))
private = hkdf(recovery_secret, EXPORT_KEM_INFO.encode("utf-8"), 32)
vectors = [
    {"name": "rfc7748_alice", "private": RFC_ALICE_PRIVATE, "public": RFC_ALICE_PUBLIC},
    {"name": "rfc7748_bob", "private": RFC_BOB_PRIVATE, "public": RFC_BOB_PUBLIC},
    {
        "name": "app:" + EXPORT_KEM_INFO,
        "recovery_secret": recovery_secret.hex(),
        "private": private.hex(),
        "is_private_clamped": clamp(private).to_bytes(32, "little") == private,
        "public": x25519(private, BASE).hex(),
    },
]

with open(sys.argv[1], "w", encoding="utf-8", newline="\n") as target:
    json.dump(vectors, target, indent=2)
    target.write("\n")
print(len(vectors), "vectors; RFC 7748 verified")
