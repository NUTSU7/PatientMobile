package com.semanticsoft.patientmobile.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthInput
import com.semanticsoft.patientmobile.ui.screens.auth.components.AuthLabel
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.Gray100
import com.semanticsoft.patientmobile.ui.theme.Gray200
import com.semanticsoft.patientmobile.ui.theme.Gray400
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import com.semanticsoft.patientmobile.ui.theme.Purple600

import com.semanticsoft.patientmobile.ui.components.PasswordCriteria
import com.semanticsoft.patientmobile.ui.components.PasswordCriteriaRow
import com.semanticsoft.patientmobile.ui.components.PasswordMatchIndicator
import com.semanticsoft.patientmobile.ui.components.computePasswordCriteria

@Composable
internal fun ChangePasswordDialog(
    email: String = "",
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
    val criteria = computePasswordCriteria(newPassword, email)

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
                        append("Schimb\u0103 ")
                    }
                    withStyle(SpanStyle(color = Purple600)) {
                        append("parola")
                    }
                },
                style = MaterialTheme.typography.titleMedium
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

            if (newPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray100, RoundedCornerShape(8.dp))
                        .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PasswordCriteriaRow(
                        met = criteria.hasLength,
                        label = "Minim 8 caractere"
                    )
                    PasswordCriteriaRow(
                        met = criteria.hasUppercase,
                        label = "Minim o liter\u0103 majuscul\u0103"
                    )
                    PasswordCriteriaRow(
                        met = criteria.hasSpecial,
                        label = "Minim un caracter special (ex: !, @, #, $, etc.)"
                    )
                    PasswordCriteriaRow(
                        met = criteria.hasDigit,
                        label = "Minim o cifr\u0103"
                    )
                    PasswordCriteriaRow(
                        met = criteria.notCommon,
                        label = "Parola nu este prea comun\u0103"
                    )
                    PasswordCriteriaRow(
                        met = criteria.notPersonal,
                        label = "Parola nu este bazat\u0103 pe date personale"
                    )
                }
            }

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

            if (confirmPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                PasswordMatchIndicator(passwordsMatch = newPassword == confirmPassword)
            }

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
                    onClick = { onSubmit(oldPassword, newPassword, confirmPassword) },
                    enabled = !isLoading && oldPassword.isNotBlank()
                        && newPassword.isNotBlank()
                        && confirmPassword.isNotBlank(),
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo600,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isLoading) "Se proceseaz\u0103..." else "Salveaz\u0103",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
