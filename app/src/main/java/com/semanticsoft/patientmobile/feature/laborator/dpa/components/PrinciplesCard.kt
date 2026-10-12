package com.semanticsoft.patientmobile.feature.laborator.dpa.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorCardTint
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorStepIconBg
import com.semanticsoft.patientmobile.core.ui.theme.TermsText
import com.semanticsoft.patientmobile.core.ui.theme.icons.CheckMarkIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.HospitalIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.InfoCircleIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.LaptopCheckIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ShieldOutlineIcon

private data class Principle(
    val icon: ImageVector,
    val iconWidth: Dp,
    val iconHeight: Dp,
    val label: String,
    val lead: String?,
    val body: String
)

private val principles = listOf(
    Principle(
        icon = HospitalIcon,
        iconWidth = 14.98.dp,
        iconHeight = 13.5.dp,
        label = "Operator de date",
        lead = "Invitro Chișinău",
        body = " păstrează și administrează datele personale ale pacienților săi."
    ),
    Principle(
        icon = LaptopCheckIcon,
        iconWidth = 16.48.dp,
        iconHeight = 13.5.dp,
        label = "Procesator digital",
        lead = "Patient",
        body = " procesează rezultate pentru a oferi experiența digitală, fără să devină depozitarul central al datelor."
    ),
    Principle(
        icon = ShieldOutlineIcon,
        iconWidth = 11.97.dp,
        iconHeight = 14.98.dp,
        label = "Confidențialitate K-Anonymity",
        lead = null,
        body = "Datele agregate raportate către voi respectă un prag minim de anonimizare clinică garantată."
    )
)

@Composable
internal fun PrinciplesCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusXLarge)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(LaboratorCardTint, shape)
            .padding(AppDimens.paddingLarge),
        verticalArrangement = Arrangement.spacedBy(AppDimens.gapLarge)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = InfoCircleIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Principii fundamentale de colaborare",
                color = Gray900,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        principles.forEach { principle ->
            PrincipleArticle(principle)
        }
    }
}

@Composable
private fun PrincipleArticle(principle: Principle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.gapLarge)
    ) {
        Box(modifier = Modifier.size(40.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LaboratorStepIconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = principle.icon,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(principle.iconWidth, principle.iconHeight)
                )
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
                    .background(Emerald600, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CheckMarkIcon,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(width = 6.78.dp, height = 5.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = principle.label.uppercase(),
                color = TermsText,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = buildAnnotatedString {
                    if (principle.lead != null) {
                        withStyle(SpanStyle(color = Indigo600, fontWeight = FontWeight.Bold)) {
                            append(principle.lead)
                        }
                    }
                    append(principle.body)
                },
                color = Gray900,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
