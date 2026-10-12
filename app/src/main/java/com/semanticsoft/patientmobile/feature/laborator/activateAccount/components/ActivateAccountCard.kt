package com.semanticsoft.patientmobile.feature.laborator.activateAccount.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.components.PasswordCriteriaRow
import com.semanticsoft.patientmobile.core.ui.components.form.FormInput
import com.semanticsoft.patientmobile.core.ui.components.GradientPillButton
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.Gray200
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray50
import com.semanticsoft.patientmobile.core.ui.theme.Gray600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.HelperTextMuted
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.PurpleLabel
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.feature.laborator.activateAccount.PasswordCriterionUi

@Composable
internal fun ActivateAccountCard(
    password: String,
    confirmPassword: String,
    criteria: List<PasswordCriterionUi>,
    termsAccepted: Boolean,
    canSubmit: Boolean,
    isLoading: Boolean,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onActivateClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Activează-ți contul de laborator",
                color = Gray900,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.7).sp,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            Text(
                text = buildAnnotatedString {
                    append("Setează o parolă pentru a accesa portalul ")
                    withStyle(SpanStyle(color = Indigo600)) { append("Patient") }
                    append(".")
                },
                color = Gray600,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Start
            )
        }

        PasswordInputGroup(
            label = "PAROLĂ",
            placeholder = "Introduceți o parolă sigură",
            value = password,
            onValueChange = onPasswordChange
        )

        PasswordInputGroup(
            label = "CONFIRMĂ PAROLĂ",
            placeholder = "Repetați parola",
            value = confirmPassword,
            imeAction = ImeAction.Done,
            onValueChange = onConfirmPasswordChange
        )

        PasswordCriteriaChecklist(criteria = criteria)

        TermsCheckboxRow(
            checked = termsAccepted,
            onCheckedChange = onTermsAcceptedChange,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick
        )

        GradientPillButton(
            text = if (isLoading) "Se activează..." else "Activează contul",
            onClick = onActivateClick,
            enabled = canSubmit && !isLoading,
            icon = null,
            fontSize = 13.sp
        )

        Text(
            text = "Completați toate câmpurile obligatorii pentru a activa contul.",
            color = HelperTextMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PasswordInputGroup(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Next
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = PurpleLabel,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.55.sp
        )
        FormInput(
            value = value,
            placeholder = placeholder,
            onValueChange = onValueChange,
            isPassword = true,
            passwordVisible = passwordVisible,
            inputHeight = AppDimens.inputHeightSmall,
            fontSize = 13.sp,
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
                imeAction = imeAction
            )
        )
    }
}

@Composable
private fun PasswordCriteriaChecklist(criteria: List<PasswordCriterionUi>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray50, RoundedCornerShape(AppDimens.cornerRadiusMedium))
            .border(1.dp, Gray300, RoundedCornerShape(AppDimens.cornerRadiusMedium))
            .padding(AppDimens.paddingDefault),
        verticalArrangement = Arrangement.spacedBy(AppDimens.gapMedium)
    ) {
        criteria.forEach { criterion ->
            PasswordCriteriaRow(
                met = criterion.met,
                label = criterion.label,
                metColor = Emerald600,
                fontSize = 13.sp
            )
        }
    }
}
