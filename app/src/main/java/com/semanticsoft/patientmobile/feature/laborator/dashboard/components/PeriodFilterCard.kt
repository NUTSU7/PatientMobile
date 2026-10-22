package com.semanticsoft.patientmobile.feature.laborator.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray300
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorBorder
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorFilterCard
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorPeriodChip

@Composable
internal fun PeriodFilterCard(
    selectedPeriod: LaboratorPeriod,
    compareEnabled: Boolean,
    onPeriodSelected: (LaboratorPeriod) -> Unit,
    onCompareToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LaboratorFilterCard(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 330.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LaboratorPeriod.entries.forEach { period ->
                    LaboratorPeriodChip(
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
