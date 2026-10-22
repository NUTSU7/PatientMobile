package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPanelBg
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.core.ui.theme.TextTertiary
import com.semanticsoft.patientmobile.core.ui.theme.icons.ArrowDownIcon
import com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.FrequentAnalysesSort

@Composable
internal fun AnalysisSortControl(
    selectedSort: FrequentAnalysesSort,
    onSortSelected: (FrequentAnalysesSort) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(37.dp)
            .background(LaboratorPanelBg, RoundedCornerShape(999.dp))
            .padding(4.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SortSegment(
            label = "REZULTATE",
            selected = selectedSort == FrequentAnalysesSort.RESULTS,
            showArrow = true,
            onClick = { onSortSelected(FrequentAnalysesSort.RESULTS) }
        )
        SortSegment(
            label = "PACIENȚI",
            selected = selectedSort == FrequentAnalysesSort.UNIQUE_PATIENTS,
            showArrow = false,
            onClick = { onSortSelected(FrequentAnalysesSort.UNIQUE_PATIENTS) }
        )
    }
}

@Composable
private fun SortSegment(
    label: String,
    selected: Boolean,
    showArrow: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    val shadowModifier = if (selected) {
        Modifier.shadow(
            elevation = 1.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.1f),
            spotColor = Color.Black.copy(alpha = 0.1f)
        )
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .then(shadowModifier)
            .clip(shape)
            .background(if (selected) SurfaceWhite else Color.Transparent, shape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Indigo600 else TextTertiary,
            fontSize = 11.sp,
            lineHeight = 16.5.sp
        )
        if (showArrow) {
            Icon(
                imageVector = ArrowDownIcon,
                contentDescription = null,
                tint = Indigo600,
                modifier = Modifier.size(9.375.dp)
            )
        }
    }
}
