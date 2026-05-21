package com.semanticsoft.patientmobile.data.remote.api

object ApiConstants {
    const val PASSWORD_MIN_LENGTH = 15
    const val PASSWORD_MAX_LENGTH = 72

    const val ACCESS_TOKEN_TTL_SECONDS = 900L
    const val REFRESH_TOKEN_TTL_SECONDS = 2_592_000L

    const val MAX_UPLOAD_BYTES = 10L * 1024L * 1024L
    val ALLOWED_UPLOAD_EXTENSIONS: Set<String> = setOf("pdf", "jpg", "jpeg", "png")

    const val DEFAULT_PAGE_SIZE = 20
    const val DEFAULT_RESULTS_PAGE_SIZE = 20

    const val OCR_POLL_DELAY_MS = 3_000L
    const val OCR_POLL_MAX_RETRIES = 40
}
