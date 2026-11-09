#!/usr/bin/env python3
"""Writes argon2id_rfc9106.json and argon2id_app_vectors.json.

A plain reading of RFC 9106 on top of hashlib.blake2b, independent of the
app code and of Bouncy Castle. It first reproduces the RFC 9106 section 5.3
pre-hashing digest and tag, and refuses to write anything if either differs.
The 64 MiB case takes about a minute.

Usage: python3 generate_argon2id_vectors.py <output-dir>
"""

import hashlib
import json
import struct
import sys
from pathlib import Path

MASK64 = (1 << 64) - 1
MASK32 = 0xFFFFFFFF
BLOCK_WORDS = 128
BLOCK_BYTES = 1024
SYNC_POINTS = 4
VERSION = 0x13
TYPE_ID = 2

ROWS = [list(range(16 * i, 16 * i + 16)) for i in range(8)]
COLUMNS = [[2 * j + 16 * r + o for r in range(8) for o in (0, 1)] for j in range(8)]
MIX = (
    (0, 4, 8, 12), (1, 5, 9, 13), (2, 6, 10, 14), (3, 7, 11, 15),
    (0, 5, 10, 15), (1, 6, 11, 12), (2, 7, 8, 13), (3, 4, 9, 14),
)

RFC_TAG = "0d640df58d78766c08c037a34a8b53c9d01ef0452d75b65eb52520e96b01e659"
RFC_PREHASH = (
    "2889de487eb42ae500c0007ed9252f1069eadec40d5765b485de6dc2437a67b8"
    "546a2f0acc1a0882db8fcf74714b472e94df421a5da1112ffa11434370a1e997"
)


def le32(value):
    return struct.pack("<I", value)


def blamka(a, b):
    return (a + b + 2 * ((a & MASK32) * (b & MASK32))) & MASK64


def rotr(x, n):
    return ((x >> n) | (x << (64 - n))) & MASK64


def permute(block, positions):
    v = [block[i] for i in positions]
    for a, b, c, d in MIX:
        v[a] = blamka(v[a], v[b])
        v[d] = rotr(v[d] ^ v[a], 32)
        v[c] = blamka(v[c], v[d])
        v[b] = rotr(v[b] ^ v[c], 24)
        v[a] = blamka(v[a], v[b])
        v[d] = rotr(v[d] ^ v[a], 16)
        v[c] = blamka(v[c], v[d])
        v[b] = rotr(v[b] ^ v[c], 63)
    for k, i in enumerate(positions):
        block[i] = v[k]


def compress(x, y):
    r = [a ^ b for a, b in zip(x, y)]
    z = list(r)
    for row in ROWS:
        permute(z, row)
    for column in COLUMNS:
        permute(z, column)
    return [a ^ b for a, b in zip(z, r)]


def h_prime(data, length):
    if length <= 64:
        return hashlib.blake2b(le32(length) + data, digest_size=length).digest()
    rounds = (length + 31) // 32 - 2
    v = hashlib.blake2b(le32(length) + data, digest_size=64).digest()
    out = v[:32]
    for _ in range(rounds - 1):
        v = hashlib.blake2b(v, digest_size=64).digest()
        out += v[:32]
    out += hashlib.blake2b(v, digest_size=length - 32 * rounds).digest()
    return out


def to_words(data):
    return list(struct.unpack("<128Q", data))


def to_bytes(words):
    return struct.pack("<128Q", *words)


def initial_hash(password, salt, secret, additional, memory_kib, iterations, lanes, tag_length):
    data = (
        le32(lanes) + le32(tag_length) + le32(memory_kib) + le32(iterations) + le32(VERSION) + le32(TYPE_ID)
        + le32(len(password)) + password + le32(len(salt)) + salt
        + le32(len(secret)) + secret + le32(len(additional)) + additional
    )
    return hashlib.blake2b(data, digest_size=64).digest()


def reference_index(pass_number, slice_number, index, same_lane, segment_length, lane_length, j1):
    if pass_number == 0:
        if slice_number == 0:
            area = index - 1
        elif same_lane:
            area = slice_number * segment_length + index - 1
        else:
            area = slice_number * segment_length - (1 if index == 0 else 0)
        start = 0
    else:
        if same_lane:
            area = lane_length - segment_length + index - 1
        else:
            area = lane_length - segment_length - (1 if index == 0 else 0)
        start = 0 if slice_number == SYNC_POINTS - 1 else (slice_number + 1) * segment_length
    x = (j1 * j1) >> 32
    y = (area * x) >> 32
    return (start + area - 1 - y) % lane_length


def argon2id(password, salt, secret, additional, memory_kib, iterations, lanes, tag_length):
    h0 = initial_hash(password, salt, secret, additional, memory_kib, iterations, lanes, tag_length)
    memory_blocks = 4 * lanes * (memory_kib // (4 * lanes))
    lane_length = memory_blocks // lanes
    segment_length = lane_length // SYNC_POINTS
    memory = [[None] * lane_length for _ in range(lanes)]
    for lane in range(lanes):
        memory[lane][0] = to_words(h_prime(h0 + le32(0) + le32(lane), BLOCK_BYTES))
        memory[lane][1] = to_words(h_prime(h0 + le32(1) + le32(lane), BLOCK_BYTES))
    zero = [0] * BLOCK_WORDS

    for pass_number in range(iterations):
        for slice_number in range(SYNC_POINTS):
            for lane in range(lanes):
                independent = pass_number == 0 and slice_number < SYNC_POINTS // 2
                address_input = [0] * BLOCK_WORDS
                address_input[0:6] = [pass_number, lane, slice_number, memory_blocks, iterations, TYPE_ID]
                addresses = None
                start_index = 2 if pass_number == 0 and slice_number == 0 else 0

                def next_addresses():
                    address_input[6] += 1
                    return compress(zero, compress(zero, address_input))

                if independent and start_index == 2:
                    addresses = next_addresses()
                for index in range(start_index, segment_length):
                    column = slice_number * segment_length + index
                    previous = memory[lane][column - 1] if column > 0 else memory[lane][lane_length - 1]
                    if independent:
                        if index % BLOCK_WORDS == 0:
                            addresses = next_addresses()
                        pseudo = addresses[index % BLOCK_WORDS]
                    else:
                        pseudo = previous[0]
                    ref_lane = lane if pass_number == 0 and slice_number == 0 else (pseudo >> 32) % lanes
                    ref_column = reference_index(
                        pass_number, slice_number, index, ref_lane == lane,
                        segment_length, lane_length, pseudo & MASK32,
                    )
                    block = compress(previous, memory[ref_lane][ref_column])
                    if pass_number > 0:
                        block = [a ^ b for a, b in zip(block, memory[lane][column])]
                    memory[lane][column] = block

    final = list(memory[0][lane_length - 1])
    for lane in range(1, lanes):
        final = [a ^ b for a, b in zip(final, memory[lane][lane_length - 1])]
    return h0, h_prime(to_bytes(final), tag_length)


def rfc_case():
    password, salt, secret, additional = b"\x01" * 32, b"\x02" * 16, b"\x03" * 8, b"\x04" * 12
    h0, tag = argon2id(password, salt, secret, additional, 32, 3, 4, 32)
    if h0.hex() != RFC_PREHASH or tag.hex() != RFC_TAG:
        sys.exit("reference implementation does not reproduce RFC 9106 section 5.3")
    return {
        "name": "rfc9106_5.3",
        "password": password.hex(),
        "salt": salt.hex(),
        "secret": secret.hex(),
        "additional": additional.hex(),
        "memory_kib": 32,
        "iterations": 3,
        "parallelism": 4,
        "tag": tag.hex(),
    }


def app_case(name, password_text, memory_kib, iterations):
    password = password_text.encode("utf-8")
    salt = bytes(range(16))
    _, tag = argon2id(password, salt, b"", b"", memory_kib, iterations, 1, 32)
    return {
        "name": name,
        "password_utf8": password.hex(),
        "salt": salt.hex(),
        "memory_kib": memory_kib,
        "iterations": iterations,
        "parallelism": 1,
        "tag": tag.hex(),
    }


def main():
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    out = Path(sys.argv[1])
    rfc = [rfc_case()]
    app = [app_case("app:production", "Parolă de test șțîâ \U0001FA7A", 65536, 3)]
    for name, cases in (("argon2id_rfc9106.json", rfc), ("argon2id_app_vectors.json", app)):
        (out / name).write_text(json.dumps(cases, indent=2, ensure_ascii=True) + "\n")


if __name__ == "__main__":
    main()
