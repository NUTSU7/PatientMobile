package com.semanticsoft.patientmobile.ui.screens.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthInput
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthLabel
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Gray400
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple600

private const val CONFIRMATION_TEXT = "\u0218TERGE CONTUL"

@Composable
internal fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onSubmit: (password: String, confirmation: String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val hasError = errorMessage != null
    val canSubmit = !isLoading && password.isNotBlank() && confirmation == CONFIRMATION_TEXT

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.paddingDefault)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append("\u0218terge ")
                    }
                    withStyle(SpanStyle(color = Purple600)) {
                        append("contul")
                    }
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            Text(
                text = "Aceast\u0103 ac\u021Biune este ireversibil\u0103. Toate datele tale medicale, analizele \u0219i istoricul vor fi \u0219terse definitiv.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            AuthLabel("Parola actual\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            AuthInput(
                value = password,
                placeholder = "Introdu parola actual\u0103",
                onValueChange = { password = it },
                isPassword = true,
                passwordVisible = passwordVisible,
                isError = hasError,
                inputHeight = AppDimens.inputHeightDefault,
                trailing = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                            tint = if (passwordVisible) Indigo600 else Gray400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            AuthLabel("Confirm\u0103 \u0219tergerea")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            AuthInput(
                value = confirmation,
                placeholder = "Scrie \u201E\u0218TERGE CONTUL\u201D",
                onValueChange = { confirmation = it },
                isPassword = false,
                isError = hasError,
                inputHeight = AppDimens.inputHeightDefault
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Anuleaz\u0103",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onSubmit(password, confirmation) },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AttentionHigh,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isLoading) "Se proceseaz\u0103..." else "\u0218terge contul",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
