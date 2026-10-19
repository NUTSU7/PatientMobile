package com.semanticsoft.patientmobile.feature.laborator.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorBadgeBg
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorRowTint
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSubtleText
import com.semanticsoft.patientmobile.core.ui.theme.PurpleLabel
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorTopAnalysis

@Composable
internal fun TopAnalysesCard(
    analyses: List<LaboratorTopAnalysis>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Top 5 analize accesate",
                color = Gray900,
                fontSize = 18.sp,
                lineHeight = 21.6.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Distribuția procentuală a interogărilor înregistrate pe portal",
                color = LaboratorSubtleText,
                fontSize = 12.sp,
                lineHeight = 16.2.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
            Text(
                text = "Sursă: Loguri criptate LIS",
                color = LaboratorSubtleText,
                fontSize = 11.sp,
                lineHeight = 16.5.sp,
                modifier = Modifier.padding(top = 10.dp)
            )

            HorizontalDivider(
                color = LaboratorBadgeBg,
                modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
            )

            analyses.forEachIndexed { index, analysis ->
                AnalysisRow(analysis = analysis)
                if (index < analyses.lastIndex) {
                    HorizontalDivider(
                        color = LaboratorBadgeBg.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisRow(
    analysis: LaboratorTopAnalysis
) {
    val rowModifier = if (analysis.insufficientData) {
        Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .background(LaboratorRowTint.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .border(1.25.dp, LaboratorBadgeBg, RoundedCornerShape(12.dp))
            .padding(12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp)
    }

    Column(modifier = rowModifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "#${analysis.rank}",
                color = LaboratorMutedText,
                fontSize = 11.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.88.sp,
                modifier = Modifier.width(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = analysis.title,
                    color = Gray900,
                    fontSize = 14.sp,
                    lineHeight = 18.9.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = analysis.subtitle,
                    color = LaboratorSubtleText,
                    fontSize = 12.sp,
                    lineHeight = 16.2.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressBar(
                percent = analysis.percent,
                insufficientData = analysis.insufficientData,
                modifier = Modifier.weight(1f)
            )
            if (analysis.percent != null) {
                Text(
                    text = "${analysis.percent}%",
                    color = PurpleLabel,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(38.dp)
                )
            } else {
                Row(
                    modifier = Modifier.width(105.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = LaboratorMutedText,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Date insuficiente",
                        color = LaboratorSubtleText,
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(
    percent: Int?,
    insufficientData: Boolean,
    modifier: Modifier = Modifier
) {
    val fraction = if (percent != null) {
        (percent / 100f).coerceIn(0.04f, 1f)
    } else {
        0.08f
    }
    val fillColor = if (insufficientData) Gray300 else Indigo600

    Box(
        modifier = modifier
            .height(9.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(LaboratorPanelBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(fillColor, RoundedCornerShape(999.dp))
        )
    }
}
