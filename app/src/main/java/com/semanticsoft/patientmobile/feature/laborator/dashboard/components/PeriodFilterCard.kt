package com.semanticsoft.patientmobile.feature.laborator.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorBorder
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.feature.laborator.dashboard.LaboratorPeriod

@Composable
internal fun PeriodFilterCard(
    selectedPeriod: LaboratorPeriod,
    compareEnabled: Boolean,
    onPeriodSelected: (LaboratorPeriod) -> Unit,
    onCompareToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, cardShape)
            .border(1.25.dp, LaboratorBorder.copy(alpha = 0.3f), cardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 330.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LaboratorPeriod.entries.forEach { period ->
                    PeriodChip(
                        label = period.label,
                        selected = selectedPeriod == period,
                        compact = compact,
                        onClick = { onPeriodSelected(period) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .background(LaboratorPanelBg, RoundedCornerShape(999.dp))
                .border(1.25.dp, LaboratorBorder.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                .padding(start = 14.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMPARĂ CU PERIOADA ANTERIOARĂ",
                color = LaboratorMutedText,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.55.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            LaboratorSwitch(
                checked = compareEnabled,
                onCheckedChange = onCompareToggle
            )
        }
    }
}

@Composable
private fun PeriodChip(
    label: String,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)
    val background = if (selected) Indigo600 else LaboratorDashboardBackground
    val contentColor = if (selected) Color.White else Indigo600
    val borderColor = if (selected) Indigo600 else Color.Transparent

    Box(
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(shape)
            .background(background, shape)
            .border(1.25.dp, borderColor, shape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = if (compact) 2.dp else 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = if (compact) 11.sp else 12.sp,
            lineHeight = if (compact) 16.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun LaboratorSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        label = "labSwitchThumb"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) Indigo600 else Gray300,
        label = "labSwitchTrack"
    )

    Box(
        modifier = Modifier
            .width(44.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = CircleShape,
                    ambientColor = Gray900.copy(alpha = 0.2f),
                    spotColor = Gray900.copy(alpha = 0.2f)
                )
                .background(Color.White, CircleShape)
        )
    }
}
