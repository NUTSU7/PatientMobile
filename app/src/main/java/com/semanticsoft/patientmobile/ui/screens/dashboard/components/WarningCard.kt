package com.semanticsoft.patientmobile.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.R
import com.semanticsoft.patientmobile.data.model.WarningCardItem
import com.semanticsoft.patientmobile.data.model.WarningIndicatorItem
import com.semanticsoft.patientmobile.data.model.WarningLevel
import com.semanticsoft.patientmobile.ui.theme.icons.WarningIcon
import kotlin.math.max
import kotlin.math.min

@Composable
fun WarningCard(
    warningCard: WarningCardItem,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = max(0.86f, min(maxWidth.value / 343.2f, 1.06f))

        val (backgroundColor, titleColor, contentColor) = when (warningCard.level) {
            WarningLevel.HIGH -> Triple(
                Color(0xFFFEF2F2),
                Color(0xFFB91C1C),
                Color(0xFF7F1D1D)
            )
            WarningLevel.MODERATE -> Triple(
                Color(0xFFFEFCE8),
                Color(0xFFA16207),
                Color(0xFF713F12)
            )
        }

        val title = when (warningCard.level) {
            WarningLevel.HIGH -> "Atenție!"
            WarningLevel.MODERATE -> "Atenție moderată"
        }

        val disclaimerText = "Vă recomandăm să consultați medicul pentru interpretare."

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape((12f * scale).dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding((20.8f * scale).dp)
            ) {
                // Header with icon and title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((28f * scale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8f * scale).dp)
                ) {
                    Icon(
                        imageVector = WarningIcon,
                        contentDescription = "Warning Icon",
                        tint = titleColor,
                        modifier = Modifier.size((20f * scale).dp)
                    )

                    Text(
                        text = title,
                        color = titleColor,
                        fontSize = (15.3f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (28f * scale).sp
                    )
                }

                Spacer(modifier = Modifier.height((12f * scale).dp))

                // Indicators list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy((6f * scale).dp)
                ) {
                    warningCard.indicators.forEach { indicator ->
                        WarningIndicatorRow(
                            indicator = indicator,
                            color = contentColor,
                            scale = scale
                        )
                    }
                }

                Spacer(modifier = Modifier.height((12f * scale).dp))

                // Disclaimer text
                Text(
                    text = disclaimerText,
                    color = titleColor,
                    fontSize = (11.9f * scale).sp,
                    lineHeight = (18f * scale).sp
                )
            }
        }
    }
}

@Composable
private fun WarningIndicatorRow(
    indicator: WarningIndicatorItem,
    color: Color,
    scale: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy((4f * scale).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = indicator.name,
            color = color,
            fontSize = (11.9f * scale).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (20f * scale).sp
        )

        Text(
            text = "•",
            color = color,
            fontSize = (11.9f * scale).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (20f * scale).sp
        )

        Text(
            text = ": ${indicator.value} ${indicator.unit}",
            color = color,
            fontSize = (11.9f * scale).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (20f * scale).sp
        )
    }
}
