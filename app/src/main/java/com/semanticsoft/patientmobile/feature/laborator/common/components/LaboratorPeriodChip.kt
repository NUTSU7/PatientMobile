package com.semanticsoft.patientmobile.feature.laborator.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground

@Composable
internal fun LaboratorPeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    horizontalPadding: Dp = 4.dp
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
            .padding(horizontal = horizontalPadding),
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
