package com.semanticsoft.patientmobile.feature.laborator.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorBorder
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorMutedText
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.feature.laborator.common.Period
import com.semanticsoft.patientmobile.feature.laborator.common.components.FilterCard
import com.semanticsoft.patientmobile.feature.laborator.common.components.PeriodChip
import com.semanticsoft.patientmobile.feature.laborator.common.components.Switch

@Composable
internal fun PeriodFilterCard(
    selectedPeriod: Period,
    compareEnabled: Boolean,
    onPeriodSelected: (Period) -> Unit,
    onCompareToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterCard(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 330.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Period.entries.forEach { period ->
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
            Switch(
                checked = compareEnabled,
                onCheckedChange = onCompareToggle
            )
        }
    }
}

