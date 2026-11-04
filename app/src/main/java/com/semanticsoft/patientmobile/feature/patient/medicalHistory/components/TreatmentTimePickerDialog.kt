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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.core.ui.theme.AppDimens
import com.semanticsoft.patientmobile.core.ui.theme.Indigo500
import com.semanticsoft.patientmobile.core.ui.theme.LaboratorDashboardBackground
import java.util.Locale

/**
 * Material 3 time picker dialog wrapper for selecting administration times.
 * Sits in-layout within screen content bounds matching the exact dimensions of [AddTreatmentDialog]
 * without obscuring topbar or bottom navigation bar.
 * Supports switching between dial clock face and text input, enforcing European 24h format.
 * Hoists selected time as "HH:mm" formatted string to [onConfirm].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TreatmentTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val initialHour = remember(initialTime) {
        initialTime.substringBefore(':').toIntOrNull() ?: 8
    }
    val initialMinute = remember(initialTime) {
        initialTime.substringAfter(':', "00").take(2).toIntOrNull() ?: 0
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    var isInputMode by rememberSaveable { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val canShowDial = configuration.screenHeightDp > 400
    val showDial = !isInputMode && canShowDial

    val timePickerColors = TimePickerDefaults.colors(
        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
        selectorColor = Indigo500,
        containerColor = LaboratorDashboardBackground,
        timeSelectorSelectedContainerColor = Indigo500.copy(alpha = 0.12f),
        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        timeSelectorSelectedContentColor = Indigo500,
        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
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
                Text(
                    text = if (showDial) "Selectează ora" else "Introdu ora",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (showDial) {
                    TimePicker(
                        state = timePickerState,
                        colors = timePickerColors
                    )
                } else {
                    TimeInput(
                        state = timePickerState,
                        colors = timePickerColors
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canShowDial) {
                    IconButton(onClick = { isInputMode = !isInputMode }) {
                        Icon(
                            imageVector = if (showDial) Icons.Outlined.Keyboard else Icons.Outlined.Schedule,
                            contentDescription = if (showDial) {
                                "Comută la introducere text"
                            } else {
                                "Comută la cadran ceas"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Row(
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
                            val formatted = String.format(
                                Locale.ROOT,
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onConfirm(formatted)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Indigo500)
                    ) {
                        Text(
                            text = "OK",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
