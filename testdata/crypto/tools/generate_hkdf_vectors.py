"""Independent HKDF-SHA256 reference (RFC 5869) built on the Python standard library only.

Usage: python3 generate_hkdf_vectors.py <output hkdf_vectors.json>
Cases 1-3 are the RFC 5869 appendix A.1-A.3 inputs and are checked against the published OKM values.
The app cases use a fixed IKM and the persisted info strings. Never feed this script from app code.
"""
import hashlib
import hmac
import json
import sys

HASH_LEN = 32


def hkdf(ikm: bytes, salt: bytes, info: bytes, length: int) -> bytes:
    prk = hmac.new(salt or bytes(HASH_LEN), ikm, hashlib.sha256).digest()
    okm = b""
    block = b""
    counter = 1
    while len(okm) < length:
        block = hmac.new(prk, block + info + bytes([counter]), hashlib.sha256).digest()
        okm += block
        counter += 1
    return okm[:length]


RFC_EXPECTED = {
    "rfc5869_case1": "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865",
    "rfc5869_case2": (
        "b11e398dc80327a1c8e7f78c596a49344f012eda2d4efad8a050cc4c19afa97c59045a99cac7827271cb41c65e590e09"
        "da3275600c2f09b8367793a9aca3db71cc30c58179ec3e87c14c01d5c1f3434f1d87"
    ),
    "rfc5869_case3": "8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d9d201395faa4b61a96c8",
}

cases = [
    ("rfc5869_case1", bytes([0x0B] * 22), bytes(range(0x00, 0x0D)), bytes(range(0xF0, 0xFA)), 42),
    ("rfc5869_case2", bytes(range(0x00, 0x50)), bytes(range(0x60, 0xB0)), bytes(range(0xB0, 0x100)), 82),
    ("rfc5869_case3", bytes([0x0B] * 22), b"", b"", 42),
]

APP_INFO = [
    "patient/recovery-wrap/v1",
    "patient/export-kem/v1",
    "patient/db/v1",
    "patient/file-kek/v1",
    "patient/export/v1",
]
APP_IKM = bytes(range(32))
for info in APP_INFO:
    cases.append(("app:" + info, APP_IKM, b"", info.encode("utf-8"), 32))

vectors = []
for name, ikm, salt, info, length in cases:
    okm = hkdf(ikm, salt, info, length).hex()
    if name in RFC_EXPECTED:
        assert okm == RFC_EXPECTED[name], name
    vectors.append(
        {"name": name, "ikm": ikm.hex(), "salt": salt.hex(), "info": info.hex(), "length": length, "okm": okm}
    )

with open(sys.argv[1], "w", encoding="utf-8", newline="\n") as target:
    json.dump(vectors, target, indent=2)
    target.write("\n")
print(len(vectors), "vectors; RFC cases verified")
