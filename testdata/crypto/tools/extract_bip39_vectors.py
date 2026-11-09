"""Extracts the 256-bit entropy/mnemonic pairs from the Trezor vectors file.

Usage: python3 extract_bip39_vectors.py <trezor vectors.json> <output.json>
Only the entropy and mnemonic columns are kept; seeds and xprv values are dropped.
"""
import json
import sys

ENTROPY_HEX_LENGTH_256 = 64

with open(sys.argv[1], encoding="utf-8") as source:
    rows = json.load(source)["english"]

pairs = [
    {"entropy": row[0], "mnemonic": row[1]}
    for row in rows
    if len(row[0]) == ENTROPY_HEX_LENGTH_256
]

with open(sys.argv[2], "w", encoding="utf-8") as target:
    json.dump(pairs, target, indent=2)
    target.write("\n")
print(len(pairs), "pairs")
