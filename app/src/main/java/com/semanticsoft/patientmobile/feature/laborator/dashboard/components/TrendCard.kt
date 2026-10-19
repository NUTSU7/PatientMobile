package com.semanticsoft.patientmobile.feature.laborator.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray500
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSubtleText
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorTrendSummary

@Composable
internal fun TrendCard(
    trend: LaboratorTrendSummary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(LaboratorPanelBg)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 58.dp, y = (-46).dp)
                .size(190.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Indigo600.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Indigo600.copy(alpha = 0.22f),
                                    Indigo600.copy(alpha = 0f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Tendințe de activitate",
                    color = Indigo600,
                    fontSize = 18.sp,
                    lineHeight = 21.6.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = buildAnnotatedString {
                    append(trend.prefix)
                    withStyle(
                        SpanStyle(
                            color = Indigo600,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(trend.highlight)
                    }
                    append(trend.suffix)
                },
                color = LaboratorSubtleText,
                fontSize = 16.sp,
                lineHeight = 23.2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .border(1.25.dp, LaboratorPanelBg, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = LaboratorMutedText,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(13.dp)
                )
                Text(
                    text = trend.footnote,
                    color = Gray500,
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
