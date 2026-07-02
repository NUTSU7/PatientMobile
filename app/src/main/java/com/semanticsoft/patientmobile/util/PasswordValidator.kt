package com.semanticsoft.patientmobile.util

import java.util.Locale

object PasswordValidator {
    private const val MIN_LENGTH = 8
    private const val MAX_UTF8_BYTES = 72

    private val commonPasswordBlocklist = setOf(
        "password",
        "password123",
        "qwerty",
        "qwerty123",
        "123456789",
        "letmein",
        "welcome",
        "iloveyou",
        "admin",
        "changeme"
    )

    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
    }

    fun validate(password: String, email: String, firstName: String, lastName: String): Result {
        if (password.length < MIN_LENGTH) {
            return Result.Error("Password must be at least 8 characters long.")
        }

        if (password.toByteArray(Charsets.UTF_8).size > MAX_UTF8_BYTES) {
            return Result.Error("Password must be at most 72 UTF-8 bytes long.")
        }

        val normalizedPassword = password.lowercase(Locale.ROOT)
        val normalizedEmail = email.lowercase(Locale.ROOT)
        val normalizedFirstName = firstName.lowercase(Locale.ROOT).trim()
        val normalizedLastName = lastName.lowercase(Locale.ROOT).trim()
        val emailLocalPart = normalizedEmail.substringBefore("@", missingDelimiterValue = "")

        val forbiddenFragments = buildSet {
            if (normalizedEmail.isNotBlank()) add(normalizedEmail)
            if (emailLocalPart.isNotBlank()) add(emailLocalPart)
            if (normalizedFirstName.isNotBlank()) add(normalizedFirstName)
            if (normalizedLastName.isNotBlank()) add(normalizedLastName)
            if (normalizedFirstName.isNotBlank() && normalizedLastName.isNotBlank()) {
                add(normalizedFirstName + normalizedLastName)
                add("$normalizedFirstName.$normalizedLastName")
                add(normalizedLastName + normalizedFirstName)
            }
        }

        if (forbiddenFragments.any { fragment -> fragment.length >= 3 && normalizedPassword.contains(fragment) }) {
            return Result.Error("Password must not match personal information.")
        }

        if (normalizedPassword in commonPasswordBlocklist) {
            return Result.Error("Password is too common or has appeared in known credential breaches.")
        }

        return Result.Success
    }
}
