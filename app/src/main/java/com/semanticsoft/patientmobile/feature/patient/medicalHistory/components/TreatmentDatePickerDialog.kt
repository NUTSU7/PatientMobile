package com.semanticsoft.patientmobile.feature.patient.medicalHistory.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Indigo500
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Material 3 date picker dialog adhering to app theme and design system tokens.
 * Sits in-layout within screen content bounds matching the exact dimensions of [AddTreatmentDialog]
 * without obscuring topbar or bottom navigation bar.
 * Uses [LaboratorDashboardBackground] container color and [Indigo500] primary highlights.
 * Hoists selected [LocalDate] to [onConfirm].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TreatmentDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val initialMillis = remember(initialDate) {
        initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )
    val isConfirmEnabled = remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    val datePickerColors = DatePickerDefaults.colors(
        containerColor = LaboratorDashboardBackground,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        headlineContentColor = MaterialTheme.colorScheme.onSurface,
        weekdayContentColor = MaterialTheme.colorScheme.onSurface,
        subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        currentYearContentColor = Indigo500,
        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedYearContainerColor = Indigo500,
        dayContentColor = MaterialTheme.colorScheme.onSurface,
        disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedDayContainerColor = Indigo500,
        todayContentColor = Indigo500,
        todayDateBorderColor = Indigo500
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = LaboratorDashboardBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.paddingDefault),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(
                    state = datePickerState,
                    title = {
                        Text(
                            text = "Selectează data de început",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    },
                    colors = datePickerColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Indigo500)
                ) {
                    Text(
                        text = "Anulează",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        val selectedDate = if (millis != null) {
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        } else {
                            initialDate
                        }
                        onConfirm(selectedDate)
                    },
                    enabled = isConfirmEnabled.value,
                    colors = ButtonDefaults.textButtonColors(contentColor = Indigo500)
                ) {
                    Text(
                        text = "Confirmă",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
