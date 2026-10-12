package com.semanticsoft.patientmobile.feature.laborator.dpa.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorBadgeBg
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.TermsText
import com.semanticsoft.patientmobile.core.ui.theme.icons.DatabaseIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.LockIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.SealCheckIcon

private data class SecurityFact(
    val icon: ImageVector,
    val iconWidth: Dp,
    val iconHeight: Dp,
    val label: String,
    val value: String
)

private val securityFacts = listOf(
    SecurityFact(
        icon = LockIcon,
        iconWidth = 13.32.dp,
        iconHeight = 17.5.dp,
        label = "Criptare completă",
        value = "AES-256 & TLS 1.3"
    ),
    SecurityFact(
        icon = DatabaseIcon,
        iconWidth = 15.dp,
        iconHeight = 15.dp,
        label = "Centru de Date",
        value = "Chisinau"
    ),
    SecurityFact(
        icon = SealCheckIcon,
        iconWidth = 18.32.dp,
        iconHeight = 17.5.dp,
        label = "Standard Conformitate",
        value = "ISO 27001 & GDPR"
    )
)

@Composable
internal fun SecurityFactsCards(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.gapDefault)
    ) {
        securityFacts.forEach { fact ->
            SecurityFactCard(fact)
        }
    }
}

@Composable
private fun SecurityFactCard(fact: SecurityFact) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusMedium)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(SurfaceWhite, shape)
            .padding(AppDimens.paddingDefault),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LaboratorBadgeBg, RoundedCornerShape(AppDimens.cornerRadiusSmall)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fact.icon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(fact.iconWidth, fact.iconHeight)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = fact.label,
                color = TermsText,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = fact.value,
                color = Gray900,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
