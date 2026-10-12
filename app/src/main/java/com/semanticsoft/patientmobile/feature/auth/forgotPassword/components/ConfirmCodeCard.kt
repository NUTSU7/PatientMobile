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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.components.form.FormInput
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Gray100
import com.semanticsoft.patientmobile.core.ui.theme.Gray200
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.InfoCardBackground
import com.semanticsoft.patientmobile.core.ui.theme.PurpleLabel
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.icons.ArrowLeftIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.EmailCheckIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ExpiryClockIcon

@Composable
internal fun ConfirmCodeCard(
    code: String,
    onCodeChange: (String) -> Unit,
    onResendClick: () -> Unit,
    onBackToLogin: () -> Unit,
    resendCooldownSeconds: Int = 0,
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
            text = "Dacă adresa introdusă are un cont Patient, ai primit un e-mail cu un cod de resetare, valabil 30 de minute.",
            color = Gray600,
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        InfoBox()

        CodeInput(
            code = code,
            onCodeChange = onCodeChange
        )

        ResendSection(
            onResendClick = onResendClick,
            cooldownSeconds = resendCooldownSeconds
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFB91C1C),
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
                imageVector = EmailCheckIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 26.dp, height = 22.dp)
            )
        }

        Text(
            text = "Verifica-ți e-mailul",
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
private fun InfoBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InfoCardBackground, RoundedCornerShape(AppDimens.cornerRadiusMedium))
            .drawBehind {
                drawLine(
                    color = Indigo600.copy(alpha = 0.25f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ExpiryClockIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(width = 12.dp, height = 14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Linkul de acces expiră în 30 de minute",
            color = Gray500,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CodeInput(
    code: String,
    onCodeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "CODUL",
            color = PurpleLabel,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.55.sp
        )
        FormInput(
            value = code,
            placeholder = "Introdu codul primit prin email",
            onValueChange = { input ->
                onCodeChange(input.filter { it.isDigit() }.take(6))
            },
            inputHeight = AppDimens.inputHeightSmall,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun ResendSection(
    onResendClick: () -> Unit,
    cooldownSeconds: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Nu ai primit e-mailul? Verifică folderul Spam sau",
            color = Gray500,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (cooldownSeconds > 0) "retrimite codul ($cooldownSeconds)s" else "retrimite codul",
                color = if (cooldownSeconds > 0) Gray500 else Indigo600,
                fontSize = 12.sp,
                modifier = Modifier.clickable(enabled = cooldownSeconds == 0, onClick = onResendClick)
            )
            Text(
                text = ".",
                color = Gray500,
                fontSize = 12.sp
            )
        }
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
            text = "Înapoi la autentificare",
            color = Gray500,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}