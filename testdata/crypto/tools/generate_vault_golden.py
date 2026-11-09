"""Writes vault_v1_golden.json, the format 1 contract file, independent of app code.

Usage: python3 generate_vault_golden.py <output vault_v1_golden.json>
Every byte string is a fixed pattern, not key material. Fields appear in the documented order,
compact JSON with no trailing newline, standard Base64 with padding.
"""
import base64
import json
import sys


def b64(length: int, start: int) -> str:
    return base64.b64encode(bytes((start + i) % 256 for i in range(length))).decode("ascii")


vault = {
    "format": 1,
    "created_at": "2026-11-06T10:00:00Z",
    "kdf": {"alg": "argon2id", "salt": b64(16, 0), "memory_kib": 65536, "iterations": 3, "parallelism": 1},
    "hw_key_alias": "patient_hw_v1",
    "wrapped_mk_password": b64(88, 16),
    "wrapped_mk_recovery": b64(60, 104),
    "wrapped_mk_biometric": None,
    "bio_key_alias": None,
    "export_public_key": b64(32, 164),
    "failed_attempts": 0,
    "wipe_after_failed_attempts": 0,
    "lockout_until_elapsed": 0,
    "lockout_boot_count": 0,
}

with open(sys.argv[1], "w", encoding="utf-8", newline="") as target:
    target.write(json.dumps(vault, separators=(",", ":"), ensure_ascii=True))
