package com.semanticsoft.patientmobile.ui.screens.uploadedAnalyses.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.AppDimens

enum class SortCriteria {
    DENUMIRE,
    DATA_INCARCARII,
    TIP,
    DIMENSIUNE;

    val displayName: String
        get() = when (this) {
            DENUMIRE -> "Denumire"
            DATA_INCARCARII -> "Data încărcării"
            TIP -> "Tip"
            DIMENSIUNE -> "Dimensiune"
        }
}

enum class SortOrder {
    CRESCATOR,
    DESCRESCATOR;

    val displayName: String
        get() = when (this) {
            CRESCATOR -> "Crescător"
            DESCRESCATOR -> "Descrescător"
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentCriteria: SortCriteria,
    currentOrder: SortOrder,
    onCriteriaSelected: (SortCriteria) -> Unit,
    onOrderSelected: (SortOrder) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = AppDimens.cornerRadiusMedium,
            topEnd = AppDimens.cornerRadiusMedium
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppDimens.paddingDefault,
                    end = AppDimens.paddingDefault,
                    top = AppDimens.paddingDefault,
                    bottom = AppDimens.paddingXXLarge
                )
        ) {
            Text(
                text = "Sortează",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            HorizontalDivider(
                modifier = Modifier.padding(bottom = AppDimens.gapDefault),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SortCriteria.entries.forEach { criteria ->
                SortOptionRow(
                    text = criteria.displayName,
                    isSelected = criteria == currentCriteria,
                    onClick = { onCriteriaSelected(criteria) }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppDimens.gapDefault),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SortOrder.entries.forEach { order ->
                SortOptionRow(
                    text = order.displayName,
                    isSelected = order == currentOrder,
                    onClick = { onOrderSelected(order) }
                )
            }
        }
    }
}

@Composable
private fun SortOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.buttonHeightDefault)
            .clickable(onClick = onClick)
            .padding(horizontal = AppDimens.paddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
