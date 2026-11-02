package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo500
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorAnalysisSubtitle
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorArticleTint
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDivider
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorEngagementGreen
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorEngagementRed
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorInsufficientText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSubtleText
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.DigitalEngagementUiState
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.EngagementLocation

private val progressBrush = Brush.horizontalGradient(
    colors = listOf(Indigo600, Indigo500)
)

@Composable
internal fun LocationEngagementCard(
    state: DigitalEngagementUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.25.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Implicarea pe locații",
                    color = Gray900,
                    fontSize = 18.sp,
                    lineHeight = 22.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Adopția digitală și prima accesare",
                    color = LaboratorAnalysisSubtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.8.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            Text(
                text = "${state.activeLocationCount} active",
                color = Indigo600,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .background(LaboratorPanelBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            )
        }

        val visibleLocations = state.visibleLocations
        if (visibleLocations.isEmpty()) {
            Text(
                text = "Nu există locații pentru filtrul selectat.",
                color = LaboratorInsufficientText,
                fontSize = 11.sp,
                lineHeight = 16.5.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 18.dp)
            ) {
                visibleLocations.forEach { location ->
                    LocationArticle(
                        location = location,
                        compareEnabled = state.compareEnabled
                    )
                }
            }
        }

        HorizontalDivider(
            color = LaboratorDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            text = state.locationFootnote,
            color = LaboratorInsufficientText,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun LocationArticle(
    location: EngagementLocation,
    compareEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor = if (location.previousChangePp < 0) {
        LaboratorEngagementRed
    } else {
        LaboratorEngagementGreen
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LaboratorArticleTint, RoundedCornerShape(13.dp))
            .border(1.25.dp, LaboratorPanelBg, RoundedCornerShape(13.dp))
            .padding(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(dotColor, CircleShape)
                )
                Text(
                    text = location.location.label,
                    color = Gray900,
                    fontSize = 13.sp,
                    lineHeight = 19.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                text = "${location.adoptionPercent}%",
                color = Gray900,
                fontSize = 13.sp,
                lineHeight = 19.5.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        LocationProgressBar(
            percent = location.adoptionPercent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )

        Row(modifier = Modifier.padding(top = 12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PRIMA ACCESARE",
                    color = LaboratorInsufficientText,
                    fontSize = 9.sp,
                    lineHeight = 13.5.sp,
                    letterSpacing = 0.36.sp
                )
                Text(
                    text = location.firstAccessLabel,
                    color = LaboratorSubtleText,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "VS. ANTERIOR",
                    color = LaboratorInsufficientText,
                    fontSize = 9.sp,
                    lineHeight = 13.5.sp,
                    letterSpacing = 0.36.sp
                )
                Text(
                    text = when {
                        !compareEnabled -> "—"
                        location.previousChangePp > 0 -> "+${location.previousChangePp} pp"
                        location.previousChangePp < 0 -> "${location.previousChangePp} pp"
                        else -> "0 pp"
                    },
                    color = if (location.previousChangePp < 0) LaboratorEngagementRed else Emerald600,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun LocationProgressBar(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val trackHeight = 7.dp
    val fillFraction = percent.coerceIn(0, 100) / 100f

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .background(LaboratorPanelBg, RoundedCornerShape(999.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fillFraction)
                .height(trackHeight)
                .background(progressBrush, RoundedCornerShape(999.dp))
        )
    }
}
