package com.estcomputer.patient.domain.port

/** SHA-256 over an audited platform implementation; implemented in `shared:crypto`. */
public interface Sha256 {
    public fun digest(input: ByteArray): ByteArray
}
