package com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorAnalysisSubtitle
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import com.semanticsoft.patientmobile.core.ui.theme.TextTertiary
import com.semanticsoft.patientmobile.core.ui.theme.icons.CheckMarkIcon
import com.semanticsoft.patientmobile.core.ui.theme.icons.ChevronDownIcon
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorFilterCard
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorPeriodChip
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorSwitch
import com.semanticsoft.patientmobile.feature.laborator.digitalEngagement.EngagementLocationFilter

@Composable
internal fun EngagementFiltersCard(
    selectedPeriod: LaboratorPeriod,
    locationFilter: EngagementLocationFilter,
    compareEnabled: Boolean,
    onPeriodSelected: (LaboratorPeriod) -> Unit,
    onLocationSelected: (EngagementLocationFilter) -> Unit,
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
                .heightIn(min = 44.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocationDropdown(
                selected = locationFilter,
                onSelected = onLocationSelected,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Compară",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    lineHeight = 16.5.sp
                )
                LaboratorSwitch(
                    checked = compareEnabled,
                    onCheckedChange = onCompareToggle
                )
            }
        }
    }
}

@Composable
private fun LocationDropdown(
    selected: EngagementLocationFilter,
    onSelected: (EngagementLocationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Locație",
            color = LaboratorAnalysisSubtitle,
            fontSize = 11.sp,
            lineHeight = 16.5.sp
        )
        Box {
            Row(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .heightIn(min = 38.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(LaboratorDashboardBackground, RoundedCornerShape(999.dp))
                    .clickable(role = Role.DropdownList, onClick = { expanded = true })
                    .padding(start = 14.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected.label,
                    color = Indigo600,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = ChevronDownIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 3.dp
            ) {
                EngagementLocationFilter.entries.forEach { filter ->
                    val isSelected = filter == selected
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = filter.label,
                                color = if (isSelected) Indigo600 else Gray900,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = CheckMarkIcon,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            null
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        onClick = {
                            expanded = false
                            onSelected(filter)
                        }
                    )
                }
            }
        }
    }
}
