package com.semanticsoft.patientmobile.data.remote.api.dto.mappers

import com.semanticsoft.patientmobile.domain.model.OcrStatus
import java.time.Instant
import java.time.LocalDate

// ── Defensive date parsers ──────────────────────────────────────────────────

@JvmSynthetic
fun parseDateSafe(raw: String?): LocalDate {
    if (raw.isNullOrBlank()) return LocalDate.EPOCH
    return runCatching {
        LocalDate.parse(raw.substringBefore('T'))
    }.getOrElse {
        try {
            LocalDate.parse(raw, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {
            try {
                LocalDate.parse(raw, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            } catch (_: Exception) {
                LocalDate.EPOCH
            }
        }
    }
}

@JvmSynthetic
fun parseInstantSafe(raw: String?): Instant {
    if (raw.isNullOrBlank()) return Instant.EPOCH
    return runCatching {
        Instant.parse(raw.substringBefore(' '))
    }.getOrElse {
        try {
            Instant.parse(raw)
        } catch (_: Exception) {
            try {
                val millis = raw.toLongOrNull()
                if (millis != null) Instant.ofEpochMilli(millis) else Instant.EPOCH
            } catch (_: Exception) {
                Instant.EPOCH
            }
        }
    }
}

@JvmSynthetic
fun mapOcrStatus(raw: String): OcrStatus = when (raw.uppercase()) {
    "SUCCESS" -> OcrStatus.COMPLETED
    "PARTIAL" -> OcrStatus.PARTIAL
    else -> try { OcrStatus.valueOf(raw.uppercase()) } catch (_: Exception) { OcrStatus.PENDING }
}
