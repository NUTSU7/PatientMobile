@file:Suppress("unused")

package com.semanticsoft.patientmobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMessage: String? = null,
    showCounter: Boolean = true,
    modifier: Modifier = Modifier
) {
    val maxBytes = 72
    val currentBytes = value.toByteArray(Charsets.UTF_8).size

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            visualTransformation = PasswordVisualTransformation(),
            isError = errorMessage != null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )

        if (showCounter) {
            Text(
                text = "$currentBytes/$maxBytes bytes",
                color = if (currentBytes <= maxBytes) Color(0xFF6B7280) else Color(0xFFB91C1C),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFB91C1C),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
