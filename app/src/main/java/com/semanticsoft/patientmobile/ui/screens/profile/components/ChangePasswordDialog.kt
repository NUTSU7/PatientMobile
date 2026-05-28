package com.semanticsoft.patientmobile.ui.screens.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AppShapes

@Composable
internal fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (oldPassword: String, newPassword: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val isSaveEnabled = oldPassword.isNotBlank()
        && newPassword.isNotBlank()
        && confirmPassword.isNotBlank()
        && newPassword == confirmPassword

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            color = MaterialTheme.colorScheme.surface,
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
                PasswordField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = "Parola actual\u0103",
                    visible = oldVisible,
                    onToggleVisibility = { oldVisible = !oldVisible }
                )
                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Parola nou\u0103",
                    visible = newVisible,
                    onToggleVisibility = { newVisible = !newVisible }
                )
                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm\u0103 parola nou\u0103",
                    visible = confirmVisible,
                    onToggleVisibility = { confirmVisible = !confirmVisible }
                )
                Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuleaz\u0103")
                    }
                    Spacer(modifier = Modifier.width(AppDimens.gapSmall))
                    Button(
                        onClick = { onSubmit(oldPassword, newPassword) },
                        enabled = isSaveEnabled,
                        shape = AppShapes.small
                    ) {
                        Text("Salveaz\u0103")
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = Icons.Outlined.RemoveRedEye,
                    contentDescription = if (visible) "Ascunde parola" else "Afi\u0219eaz\u0103 parola",
                    tint = if (visible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        modifier = modifier.fillMaxWidth()
    )
}
