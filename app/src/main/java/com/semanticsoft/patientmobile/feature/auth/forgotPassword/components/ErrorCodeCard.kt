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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.components.auth.GradientPillButton
import com.semanticsoft.patientmobile.core.ui.theme.AmberLightBg
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Blue900
import com.semanticsoft.patientmobile.core.ui.theme.Gray100
import com.semanticsoft.patientmobile.core.ui.theme.Gray200
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Gray600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.icons.ArrowClockwiseIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ArrowLeftIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ClockResetIcon

@Composable
internal fun ErrorCodeCard(
    onRequestNewCode: () -> Unit,
    onBackToLogin: () -> Unit,
    isLoading: Boolean = false,
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
            text = "Acest cod de resetare nu mai este valabil. Din motive de securitate, codul expiră după 30 de minute sau după prima utilizare.",
            color = Gray600,
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        GradientPillButton(
            text = if (resendCooldownSeconds > 0) "Solicită un cod nou ($resendCooldownSeconds)s" else "Solicită un cod nou",
            onClick = onRequestNewCode,
            icon = ArrowClockwiseIcon,
            enabled = !isLoading && resendCooldownSeconds == 0
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
                    elevation = 1.dp,
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    ambientColor = Color.Black.copy(alpha = 0.05f),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .background(AmberLightBg, RoundedCornerShape(AppDimens.cornerRadiusSmall))
                .border(1.dp, Blue900, RoundedCornerShape(AppDimens.cornerRadiusSmall))
                .padding(10.dp)
        ) {
            Icon(
                imageVector = ClockResetIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 26.dp, height = 26.dp)
            )
        }

        Text(
            text = "Codul a expirat",
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
            text = "Înapoi la pagina de autentificare",
            color = Gray500,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}