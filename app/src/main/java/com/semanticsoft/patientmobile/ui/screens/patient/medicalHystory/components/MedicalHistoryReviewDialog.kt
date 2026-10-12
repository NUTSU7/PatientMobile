package com.semanticsoft.patientmobile.ui.screens.patient.medicalHystory.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.Indigo600
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryReviewDialog(
    initialDisplayTitle: String,
    initialDisplaySubtitle: String,
    initialSourceLaboratory: String,
    initialObservedAt: LocalDate?,
    isSaving: Boolean,
    errorText: String?,
    onDismiss: () -> Unit,
    onSave: (displayTitle: String, displaySubtitle: String, sourceLaboratory: String, observedAt: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayTitle by rememberSaveable { mutableStateOf("") }
    var displaySubtitle by rememberSaveable { mutableStateOf("") }
    var sourceLaboratory by rememberSaveable { mutableStateOf("") }
    var observedAt by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialDisplayTitle) { displayTitle = initialDisplayTitle }
    LaunchedEffect(initialDisplaySubtitle) { displaySubtitle = initialDisplaySubtitle }
    LaunchedEffect(initialSourceLaboratory) { sourceLaboratory = initialSourceLaboratory }
    LaunchedEffect(initialObservedAt) { observedAt = initialObservedAt }

    val isError = errorText != null

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(AppDimens.paddingDefault)
        ) {
            Text(
                text = "Revizuire analiz\u0103",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Titlu analiz\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogTextField(
                value = displayTitle,
                onValueChange = { displayTitle = it },
                placeholder = "Ex: Eco-Doppler carotidian transcranian",
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Subtitlu / context")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogTextField(
                value = displaySubtitle,
                onValueChange = { displaySubtitle = it },
                placeholder = "Ex: Investiga\u021Bie vascular\u0103",
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Laborator / clinic\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogTextField(
                value = sourceLaboratory,
                onValueChange = { sourceLaboratory = it },
                placeholder = "Ex: Synevo, Artis Pol Clinica",
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Data efectu\u0103rii analizei")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogTextField(
                value = observedAt?.let { formatLocalDate(it) } ?: "",
                onValueChange = {},
                placeholder = "Selecteaz\u0103 data",
                isError = isError,
                enabled = false,
                modifier = Modifier.clickable { showDatePicker = true }
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Anuleaz\u0103",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        onSave(
                            displayTitle,
                            displaySubtitle,
                            sourceLaboratory,
                            observedAt?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toString()
                        )
                    },
                    enabled = !isSaving,
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo600,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isSaving) "Se salveaz\u0103..." else "Salveaz\u0103",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = observedAt?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            observedAt = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm\u0103")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuleaz\u0103")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatLocalDate(date: LocalDate): String {
    val monthName = when (date.monthValue) {
        1 -> "ianuarie"
        2 -> "februarie"
        3 -> "martie"
        4 -> "aprilie"
        5 -> "mai"
        6 -> "iunie"
        7 -> "iulie"
        8 -> "august"
        9 -> "septembrie"
        10 -> "octombrie"
        11 -> "noiembrie"
        12 -> "decembrie"
        else -> ""
    }
    return "${date.dayOfMonth} $monthName ${date.year}"
}
