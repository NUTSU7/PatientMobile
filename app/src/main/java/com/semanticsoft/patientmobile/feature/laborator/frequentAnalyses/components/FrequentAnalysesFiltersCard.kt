package com.semanticsoft.patientmobile.feature.laborator.frequentAnalyses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.feature.laborator.common.LaboratorPeriod
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorFilterCard
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorPeriodChip
import com.semanticsoft.patientmobile.feature.laborator.common.components.LaboratorSearchField

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

        LaboratorSearchField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

