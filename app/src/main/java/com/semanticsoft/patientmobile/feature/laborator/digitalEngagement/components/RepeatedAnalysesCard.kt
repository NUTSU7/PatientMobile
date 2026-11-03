package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Emerald600
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorAnalysisSubtitle
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDivider
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorEngagementRed
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorSubtleText
import com.semanticsoft.patientmobile.core.ui.theme.icons.ChevronDownIcon
import com.semanticsoft.patientmobile.feature.laborator.common.components.SearchField
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.DigitalEngagementUiState
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.EngagementRepeatedAnalysis

@Composable
internal fun RepeatedAnalysesCard(
    state: DigitalEngagementUiState,
    onQueryChange: (String) -> Unit,
    onAnalysisToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.25.dp, Color(0xFFE2E8F0).copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "Repetarea analizelor",
            color = Gray900,
            fontSize = 18.sp,
            lineHeight = 22.5.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Frecvența și intervalul de repetare",
            color = LaboratorAnalysisSubtitle,
            fontSize = 12.sp,
            lineHeight = 16.8.sp,
            modifier = Modifier.padding(top = 5.dp)
        )

        SearchField(
            value = state.repeatedSearchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        val analyses = state.filteredRepeatedAnalyses
        if (analyses.isEmpty()) {
            Text(
                text = "Nicio analiză găsită pentru căutarea curentă.",
                color = LaboratorAnalysisSubtitle,
                fontSize = 11.sp,
                lineHeight = 16.5.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                analyses.forEachIndexed { index, analysis ->
                    RepeatedAnalysisRow(
                        analysis = analysis,
                        expanded = state.expandedAnalysisId == analysis.id,
                        compareEnabled = state.compareEnabled,
                        onToggle = { onAnalysisToggle(analysis.id) }
                    )
                    if (index != analyses.lastIndex) {
                        HorizontalDivider(color = LaboratorDivider, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RepeatedAnalysisRow(
    analysis: EngagementRepeatedAnalysis,
    expanded: Boolean,
    compareEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = analysis.name,
                    color = Gray900,
                    fontSize = 13.sp,
                    lineHeight = 17.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = analysis.method,
                    color = LaboratorSubtleText,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Icon(
                imageVector = ChevronDownIcon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(12.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
            Text(
                text = "${analysis.repeatPercent}%",
                color = Gray900,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            RepeatedAnalysisDetail(
                analysis = analysis,
                compareEnabled = compareEnabled,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
    }
}

@Composable
private fun RepeatedAnalysisDetail(
    analysis: EngagementRepeatedAnalysis,
    compareEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LaboratorDashboardBackground, RoundedCornerShape(11.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "INTERVAL MEDIAN",
                    color = LaboratorAnalysisSubtitle,
                    fontSize = 9.sp,
                    lineHeight = 13.5.sp,
                    letterSpacing = 0.36.sp
                )
                Text(
                    text = analysis.medianInterval,
                    color = LaboratorSubtleText,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DISTRIBUȚIE",
                    color = LaboratorAnalysisSubtitle,
                    fontSize = 9.sp,
                    lineHeight = 13.5.sp,
                    letterSpacing = 0.36.sp
                )
                Text(
                    text = analysis.distribution,
                    color = Indigo600,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "VS. ANTERIOR",
                    color = LaboratorAnalysisSubtitle,
                    fontSize = 9.sp,
                    lineHeight = 13.5.sp,
                    letterSpacing = 0.36.sp
                )
                Text(
                    text = when {
                        !compareEnabled -> "—"
                        analysis.previousChangePp > 0 -> "+${analysis.previousChangePp} pp"
                        analysis.previousChangePp < 0 -> "${analysis.previousChangePp} pp"
                        else -> "0 pp"
                    },
                    color = if (analysis.previousChangePp < 0) LaboratorEngagementRed else Emerald600,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}
