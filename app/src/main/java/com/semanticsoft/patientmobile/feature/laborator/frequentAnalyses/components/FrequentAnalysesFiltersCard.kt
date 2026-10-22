package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.core.ui.theme.Gray900
import com.semanticsoft.patientmobile.core.ui.theme.Indigo600
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorInputBorder
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorPlaceholderText
import com.semanticsoft.patientmobile.core.ui.theme.SurfaceWhite
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorFilterCard
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorPeriodChip

@Composable
internal fun FrequentAnalysesFiltersCard(
    selectedPeriod: LaboratorPeriod,
    query: String,
    onPeriodSelected: (LaboratorPeriod) -> Unit,
    onQueryChange: (String) -> Unit,
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

        AnalysisSearchField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AnalysisSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)

    Row(
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = 1.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.02f),
                spotColor = Color.Black.copy(alpha = 0.02f)
            )
            .clip(shape)
            .background(SurfaceWhite)
            .border(1.25.dp, LaboratorInputBorder, shape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "Caută analiză...",
                    color = LaboratorPlaceholderText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Gray900,
                    fontSize = 14.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                cursorBrush = SolidColor(Indigo600),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Caută analiză" }
            )
        }
    }
}
