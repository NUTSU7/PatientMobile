package com.semanticsoft.patientmobile.ui.screens.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthInput
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthLabel
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AppShapes
import com.semanticsoft.patientmobile.ui.theme.Gray400
import com.semanticsoft.patientmobile.ui.theme.Indigo500
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.LightPurpleBg

@Composable
internal fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (oldPassword: String, newPassword: String, confirmPassword: String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val hasError = errorMessage != null

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = LightPurpleBg,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.paddingDefault)
        ) {
            Text(
                text = "Schimb\u0103 parola",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            AuthLabel("Parola actual\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            AuthInput(
                value = oldPassword,
                placeholder = "Introdu parola actual\u0103",
                onValueChange = { oldPassword = it },
                isPassword = true,
                passwordVisible = oldVisible,
                isError = hasError,
                inputHeight = AppDimens.inputHeightDefault,
                trailing = {
                    IconButton(onClick = { oldVisible = !oldVisible }) {
                        Icon(
                            imageVector = if (oldVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (oldVisible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                            tint = if (oldVisible) Indigo600 else Gray400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            AuthLabel("Parola nou\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            AuthInput(
                value = newPassword,
                placeholder = "Introdu parola nou\u0103",
                onValueChange = { newPassword = it },
                isPassword = true,
                passwordVisible = newVisible,
                isError = hasError,
                inputHeight = AppDimens.inputHeightDefault,
                trailing = {
                    IconButton(onClick = { newVisible = !newVisible }) {
                        Icon(
                            imageVector = if (newVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (newVisible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                            tint = if (newVisible) Indigo600 else Gray400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            AuthLabel("Confirm\u0103 parola nou\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            AuthInput(
                value = confirmPassword,
                placeholder = "Reintrodu parola nou\u0103",
                onValueChange = { confirmPassword = it },
                isPassword = true,
                passwordVisible = confirmVisible,
                isError = hasError,
                inputHeight = AppDimens.inputHeightDefault,
                trailing = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (confirmVisible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                            tint = if (confirmVisible) Indigo600 else Gray400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
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
            Button(
                onClick = { onSubmit(oldPassword, newPassword, confirmPassword) },
                modifier = Modifier.fillMaxWidth().height(AppDimens.buttonHeightDefault),
                enabled = !isLoading && oldPassword.isNotBlank()
                    && newPassword.isNotBlank()
                    && confirmPassword.isNotBlank(),
                shape = AppShapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Indigo600,
                    contentColor = Color.White
                )
            ) {
                Text(if (isLoading) "Se proceseaz\u0103..." else "Salveaz\u0103")
            }
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(AppDimens.buttonHeightDefault)
            ) {
                Text("Anuleaz\u0103", color = Indigo500)
            }
        }
    }
}
