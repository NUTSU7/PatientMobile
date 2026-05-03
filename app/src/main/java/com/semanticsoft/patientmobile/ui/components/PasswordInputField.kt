@file:Suppress("unused")

package com.semanticsoft.patientmobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMessage: String? = null,
    showCounter: Boolean = true,
    modifier: Modifier = Modifier
) {
    PasswordField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        errorMessage = errorMessage,
        showCounter = showCounter,
        modifier = modifier
    )
}
