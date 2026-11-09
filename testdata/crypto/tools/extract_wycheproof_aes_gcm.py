"""Extracts the AES-256-GCM cases with a 96-bit IV and a 128-bit tag from Wycheproof.

Usage: python3 extract_wycheproof_aes_gcm.py <wycheproof aes_gcm_test.json> <output.json>
Only the groups matching the app's AEAD parameters are kept; every field of each case is copied unchanged.
"""
import json
import sys

KEY_BITS = 256
IV_BITS = 96
TAG_BITS = 128

with open(sys.argv[1], encoding="utf-8") as source:
    groups = json.load(source)["testGroups"]

cases = [
    case
    for group in groups
    if (group["keySize"], group["ivSize"], group["tagSize"]) == (KEY_BITS, IV_BITS, TAG_BITS)
    for case in group["tests"]
]

with open(sys.argv[2], "w", encoding="utf-8", newline="\n") as target:
    json.dump(cases, target, indent=2)
    target.write("\n")
print(len(cases), "cases")
