package com.semanticsoft.patientmobile.util

import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun validate_returnsSuccess_forStrongPassword() {
        val result = PasswordValidator.validate(
            password = "Sup3rStrongPassword!",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        assertTrue(result is PasswordValidator.Result.Success)
    }

    @Test
    fun validate_returnsError_forShortPassword() {
        val result = PasswordValidator.validate(
            password = "short",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        assertTrue(result is PasswordValidator.Result.Error)
    }

    @Test
    fun validate_returnsError_whenContainsPersonalData() {
        val result = PasswordValidator.validate(
            password = "johnpasswordsecure",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        assertTrue(result is PasswordValidator.Result.Error)
    }
}
