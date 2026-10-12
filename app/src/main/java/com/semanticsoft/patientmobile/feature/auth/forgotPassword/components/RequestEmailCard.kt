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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.semanticsoft.patientmobile.core.ui.components.auth.AuthInput
import com.semanticsoft.patientmobile.core.ui.components.auth.GradientPillButton
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Gray100
import com.semanticsoft.patientmobile.core.ui.theme.Gray200
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.PurpleLabel
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.icons.EnvelopeIcon

@Composable
internal fun RequestEmailCard(
    email: String,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
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
            text = "Introdu adresa de e-mail asociată contului tău și îți trimitem un link de resetare.",
            color = Gray600,
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        EmailInput(
            email = email,
            onEmailChange = onEmailChange
        )

        GradientPillButton(text = "Trimite link de resetare", onClick = onSubmit)

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(color = Gray100)

        Spacer(modifier = Modifier.height(6.dp))

        BackToLoginRow(onBackToLogin = onBackToLogin)
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
                imageVector = EnvelopeIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 17.dp, height = 13.dp)
            )
        }

        Text(
            text = "Recuperare parolă",
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
private fun EmailInput(
    email: String,
    onEmailChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "ADRESĂ DE E-MAIL",
            color = PurpleLabel,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.55.sp
        )
        AuthInput(
            value = email,
            placeholder = "ex: nume@email.com",
            onValueChange = onEmailChange,
            inputHeight = AppDimens.inputHeightSmall,
            leading = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = Gray600,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun BackToLoginRow(onBackToLogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Îți amintești parola? ",
            color = Gray500,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Accesează contul",
            color = Indigo600,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(onClick = onBackToLogin)
        )
    }
}