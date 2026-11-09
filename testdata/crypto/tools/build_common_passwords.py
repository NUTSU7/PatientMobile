"""Builds the bundled password blocklist from the SecLists 10k file plus the app words.

Usage: python3 build_common_passwords.py <10k-most-common.txt> <output common_passwords.txt>
Entries are NFKC-normalised, lowercased, deduplicated and sorted by UTF-16 code unit.
Every entry must be ASCII so Python's lower() equals Kotlin's per-char lowercaseChar(). Entries shorter than
the minimum password length are dropped because the validator rejects those before the blocklist.
"""
import sys
import unicodedata

MIN_CODE_POINTS = 8
APP_WORDS = ["parola123", "patient123", "pacient123", "parola1234", "parolamea"]

with open(sys.argv[1], encoding="utf-8") as source:
    entries = {unicodedata.normalize("NFKC", line.strip()).lower() for line in source}
entries.update(APP_WORDS)
entries = {e for e in entries if len(e) >= MIN_CODE_POINTS}
for entry in entries:
    assert entry.isascii(), "non-ASCII entry in blocklist source"

with open(sys.argv[2], "w", encoding="utf-8", newline="\n") as target:
    for entry in sorted(entries, key=lambda e: e.encode("utf-16-be")):
        target.write(entry + "\n")
print(len(entries), "entries")
