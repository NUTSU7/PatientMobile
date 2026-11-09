package com.estcomputer.patient.domain.util

/** Logging port. Events carry only a [LogCode], so no free text can leak health data or secrets. */
public interface PatientLog {
    public fun event(code: LogCode)
}

/** Technical event codes; add entries only for events that carry no data. */
public enum class LogCode
