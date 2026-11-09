"""Builds the bundled password blocklist from the SecLists 10k file plus the app words.

Usage: python3 build_common_passwords.py <10k-most-common.txt> <output common_passwords.txt>
Entries are NFKC-normalised, lowercased, deduplicated and sorted by UTF-16 code unit.
"""
import sys
import unicodedata

APP_WORDS = ["patient", "parola123"]

with open(sys.argv[1], encoding="utf-8") as source:
    entries = {unicodedata.normalize("NFKC", line.strip()).lower() for line in source}
entries.update(APP_WORDS)
entries.discard("")

with open(sys.argv[2], "w", encoding="utf-8", newline="\n") as target:
    for entry in sorted(entries, key=lambda e: e.encode("utf-16-be")):
        target.write(entry + "\n")
print(len(entries), "entries")
