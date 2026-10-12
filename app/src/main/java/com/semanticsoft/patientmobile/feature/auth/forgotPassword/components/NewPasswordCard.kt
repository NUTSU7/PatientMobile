package com.semanticsoft.patientmobile.feature.auth.forgotPassword.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.components.PasswordCriteriaRow
import com.semanticsoft.patientmobile.core.ui.components.form.FormInput
import com.semanticsoft.patientmobile.core.ui.components.GradientPillButton
import com.semanticsoft.patientmobile.core.ui.components.computePasswordCriteria
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.ErrorText
import com.semanticsoft.patientmobile.core.ui.theme.Gray100
import com.semanticsoft.patientmobile.core.ui.theme.Gray200
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray50
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.PurpleLabel
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.icons.ArrowLeftIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.KeyholeIcon

@Composable
internal fun NewPasswordCard(
    newPassword: String,
    confirmPassword: String,
    email: String,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(AppDimens.cornerRadiusLarge)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.03f),
                spotColor = Color.Black.copy(alpha = 0.03f)
            )
            .background(SurfaceWhite, cardShape)
            .border(1.dp, Gray200, cardShape)
            .padding(horizontal = AppDimens.paddingXLarge, vertical = AppDimens.paddingXXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
    ) {
        HeaderRow()

        Text(
            text = "Alege o parolă nouă pentru contul tău.",
            color = Gray600,
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        NewPasswordInput(
            password = newPassword,
            onPasswordChange = onNewPasswordChange
        )

        PasswordChecklist(
            password = newPassword,
            email = email
        )

        ConfirmPasswordInput(
            confirmPassword = confirmPassword,
            newPassword = newPassword,
            onConfirmPasswordChange = onConfirmPasswordChange
        )

        GradientPillButton(
            text = "Schimbă parola",
            onClick = onSubmit,
            enabled = !isLoading
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = ErrorText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(color = Gray100)

        Spacer(modifier = Modifier.height(6.dp))

        BackToLoginLink(onBackToLogin = onBackToLogin)
    }
}

@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    ambientColor = Color.Black.copy(alpha = 0.05f),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .border(1.dp, Blue900, RoundedCornerShape(AppDimens.cornerRadiusSmall))
                .padding(10.dp)
        ) {
            Icon(
                imageVector = KeyholeIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 17.dp, height = 17.dp)
            )
        }

        Text(
            text = "Setează o parolă nouă",
            color = Gray900,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.66).sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NewPasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "PAROLĂ NOUĂ",
            color = PurpleLabel,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.55.sp
        )
        FormInput(
            value = password,
            placeholder = "Introdu o parolă puternică",
            onValueChange = onPasswordChange,
            isPassword = true,
            passwordVisible = passwordVisible,
            inputHeight = AppDimens.inputHeightSmall,
            trailing = {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ascunde parola" else "Afișează parola",
                    tint = Gray600,
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )
    }
}

@Composable
private fun PasswordChecklist(
    password: String,
    email: String
) {
    val criteria = computePasswordCriteria(password, email)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(AppDimens.cornerRadiusMedium))
            .border(1.dp, Gray300, RoundedCornerShape(AppDimens.cornerRadiusMedium))
            .padding(AppDimens.paddingDefault),
        verticalArrangement = Arrangement.spacedBy(AppDimens.gapMedium)
    ) {
        PasswordCriteriaRow(met = criteria.hasLength, label = "Minim 8 caractere", metColor = Emerald600)
        PasswordCriteriaRow(met = criteria.hasUppercase, label = "Minim o literă majusculă", metColor = Emerald600)
        PasswordCriteriaRow(met = criteria.hasSpecial, label = "Minim un caracter special (ex: !, @, #, $, etc.)", metColor = Emerald600)
        PasswordCriteriaRow(met = criteria.hasDigit, label = "Minim o cifră", metColor = Emerald600)
        PasswordCriteriaRow(met = criteria.notCommon, label = "Parola nu este prea comună", metColor = Emerald600)
        PasswordCriteriaRow(met = criteria.notPersonal, label = "Parola nu este bazată pe date personale", metColor = Emerald600)
    }
}

@Composable
private fun ConfirmPasswordInput(
    confirmPassword: String,
    newPassword: String,
    onConfirmPasswordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "CONFIRMĂ PAROLA NOUĂ",
            color = PurpleLabel,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.55.sp
        )
        FormInput(
            value = confirmPassword,
            placeholder = "Confirmă parola",
            onValueChange = onConfirmPasswordChange,
            isPassword = true,
            inputHeight = AppDimens.inputHeightSmall,
            leading = if (confirmPassword.isNotBlank()) ({
                val matches = confirmPassword == newPassword
                Icon(
                    imageVector = if (matches) Icons.Outlined.Check else Icons.Outlined.Close,
                    contentDescription = if (matches) "Parolele coincid" else "Parolele nu coincid",
                    tint = if (matches) Emerald600 else ErrorText,
                    modifier = Modifier.size(20.dp)
                )
            }) else null
        )
    }
}

@Composable
private fun BackToLoginLink(onBackToLogin: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onBackToLogin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = ArrowLeftIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = "Anulează și întoarce-te la autentificare",
            color = Gray500,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}