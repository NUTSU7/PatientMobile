package com.semanticsoft.patientmobile.ui.screens.medicalHystory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semanticsoft.patientmobile.domain.model.DoseUnit
import com.semanticsoft.patientmobile.domain.model.MealRelation
import com.semanticsoft.patientmobile.domain.model.PatientDocument
import com.semanticsoft.patientmobile.ui.theme.AppDimens
import com.semanticsoft.patientmobile.ui.theme.AttentionHigh
import com.semanticsoft.patientmobile.ui.theme.Gray50
import com.semanticsoft.patientmobile.ui.theme.Gray200
import com.semanticsoft.patientmobile.ui.theme.Gray500
import com.semanticsoft.patientmobile.ui.theme.Gray900
import com.semanticsoft.patientmobile.ui.theme.Indigo600

data class ScheduleEntryData(
    val administrationTime: String,
    val mealRelation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    errorText: String?,
    documents: List<PatientDocument>,
    initialAnalysisDocumentId: String? = null,
    initialAnalysisDocumentName: String? = null,
    initialName: String? = null,
    initialDoseValue: Double? = null,
    initialDoseUnit: String? = null,
    initialSchedules: List<ScheduleEntryData>? = null,
    initialDurationDays: Int? = null,
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        doseValue: Double,
        doseUnit: String,
        schedules: List<ScheduleEntryData>,
        durationDays: Int,
        associatedDocumentId: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var doseValue by rememberSaveable { mutableStateOf(1.0) }
    var selectedUnit by rememberSaveable { mutableStateOf(DoseUnit.CAPSULE) }
    var durationDays by rememberSaveable { mutableStateOf(0) }
    var associatedDocumentId by rememberSaveable { mutableStateOf(initialAnalysisDocumentId.orEmpty()) }
    var associatedDocumentLabel by rememberSaveable { mutableStateOf(initialAnalysisDocumentName.orEmpty()) }

    LaunchedEffect(initialName) { initialName?.let { name = it } }
    LaunchedEffect(initialDoseValue) { initialDoseValue?.let { doseValue = it } }
    LaunchedEffect(initialDoseUnit) {
        initialDoseUnit?.let { unitStr ->
            selectedUnit = try { DoseUnit.valueOf(unitStr) } catch (_: Exception) { DoseUnit.CAPSULE }
        }
    }
    LaunchedEffect(initialDurationDays) { initialDurationDays?.let { durationDays = it } }
    LaunchedEffect(initialAnalysisDocumentId, initialAnalysisDocumentName) {
        initialAnalysisDocumentId?.let { associatedDocumentId = it }
        initialAnalysisDocumentName?.let { associatedDocumentLabel = it }
    }

    val schedules = remember {
        mutableStateListOf(
            LocalScheduleEntry(hour = "08", minute = "00", mealRelation = MealRelation.AFTER_MEAL.name)
        )
    }
    LaunchedEffect(initialSchedules) {
        initialSchedules?.let { list ->
            schedules.clear()
            list.forEach { entry ->
                val parts = entry.administrationTime.split(":")
                schedules.add(
                    LocalScheduleEntry(
                        hour = parts.getOrElse(0) { "08" }.padStart(2, '0'),
                        minute = parts.getOrElse(1) { "00" }.padStart(2, '0'),
                        mealRelation = entry.mealRelation
                    )
                )
            }
        }
    }

    val doseUnitOptions = DoseUnit.entries.map { unit ->
        DialogDropdownOption(key = unit.name, label = unit.toRoLabel())
    }

    val documentOptions = buildList {
        add(DialogDropdownOption(key = "", label = "F\u0103r\u0103 analiz\u0103 asociat\u0103"))
        if (initialAnalysisDocumentId != null && initialAnalysisDocumentId.isNotEmpty()) {
            val inList = documents.any { it.id == initialAnalysisDocumentId }
            if (!inList && initialAnalysisDocumentName != null) {
                add(DialogDropdownOption(key = initialAnalysisDocumentId, label = "${initialAnalysisDocumentName} (indisponibil\u0103)"))
            }
        }
        addAll(documents.map { doc ->
            DialogDropdownOption(key = doc.id, label = doc.originalFileName)
        })
    }

    val mealRelationOptions = listOf(
        DialogDropdownOption(MealRelation.BEFORE_MEAL.name, "\u00CEnainte de mas\u0103"),
        DialogDropdownOption(MealRelation.WITH_MEAL.name, "\u00CEn timpul mesei"),
        DialogDropdownOption(MealRelation.AFTER_MEAL.name, "Dup\u0103 mas\u0103"),
        DialogDropdownOption(MealRelation.NO_MEAL_RELATION.name, "F\u0103r\u0103 leg\u0103tur\u0103 cu masa")
    )

    val isError = errorText != null
    var timePickerTargetIndex by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.paddingDefault)
        ) {
            // === FIXED HEADER ===
            Text(
                text = if (isEditing) "Editeaz\u0103 medicament" else "Adaug\u0103 medicament",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            // === SCROLLABLE CONTENT ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

            DialogFieldLabel("Analiz\u0103 asociat\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogDropdownField(
                selectedText = associatedDocumentLabel,
                placeholder = "F\u0103r\u0103 analiz\u0103 asociat\u0103",
                options = documentOptions,
                onSelect = { option ->
                    if (option.key.isEmpty()) {
                        associatedDocumentId = ""
                        associatedDocumentLabel = ""
                    } else {
                        associatedDocumentId = option.key
                        associatedDocumentLabel = option.label
                        if (name.isEmpty()) {
                            name = option.label
                        }
                    }
                },
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Denumire medicament")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Ex: Augmentin, Paracetamol...",
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Doz\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.gapSmall)) {
                DialogHorizontalStepperField(
                    value = doseValue,
                    onValueChange = { doseValue = it },
                    step = 0.5,
                    min = 0.0,
                    modifier = Modifier.fillMaxWidth(0.5f),
                    isError = isError
                )
                DialogDropdownField(
                    selectedText = selectedUnit.toRoLabel(),
                    placeholder = "Unitate",
                    options = doseUnitOptions,
                    onSelect = { selectedUnit = DoseUnit.valueOf(it.key) },
                    modifier = Modifier.weight(1f),
                    isError = isError
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.gapDefault))

            DialogFieldLabel("Durat\u0103")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            DialogHorizontalStepperField(
                value = durationDays.toDouble(),
                onValueChange = { durationDays = it.toInt() },
                step = 1.0,
                min = 0.0,
                placeholder = "F\u0103r\u0103 limit\u0103",
                trailingText = "zile",
                modifier = Modifier.fillMaxWidth(0.65f),
                isError = isError
            )

            Spacer(modifier = Modifier.height(AppDimens.gapLarge))

            DialogFieldLabel("Ore administrare")
            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.cornerRadiusSmall))
                    .background(Gray50)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Adaug\u0103 una sau mai multe ore zilnice \u00EEn format 24h \u0219i alege rela\u021Bia cu masa.",
                        color = Gray500,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(AppDimens.gapDefault))

                    for (i in schedules.indices) {
                        val s = schedules[i]
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(AppDimens.inputHeightSmall)
                                        .background(Color.White, RoundedCornerShape(AppDimens.cornerRadiusSmall))
                                        .border(BorderStroke(1.dp, Gray200), RoundedCornerShape(AppDimens.cornerRadiusSmall)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = s.hour, color = Gray900, fontSize = 14.sp)
                                }
                                Text(":", color = Gray900, fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(AppDimens.inputHeightSmall)
                                        .background(Color.White, RoundedCornerShape(AppDimens.cornerRadiusSmall))
                                        .border(BorderStroke(1.dp, Gray200), RoundedCornerShape(AppDimens.cornerRadiusSmall)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = s.minute, color = Gray900, fontSize = 14.sp)
                                }
                                IconButton(
                                    onClick = { schedules.removeAt(i) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Elimin\u0103",
                                        tint = AttentionHigh,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                            OutlinedButton(
                                onClick = { timePickerTargetIndex = i },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Indigo600
                                ),
                                border = BorderStroke(1.dp, Indigo600),
                                shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Selecteaz\u0103 ora",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                            DialogCompactDropdownField(
                                selectedText = mealRelationOptions.find { it.key == s.mealRelation }?.label ?: "",
                                options = mealRelationOptions,
                                onSelect = { schedules[i] = s.copy(mealRelation = it.key) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(AppDimens.gapSmall))
                    }

                    TextButton(
                        onClick = {
                            schedules.add(
                                LocalScheduleEntry(
                                    hour = "08",
                                    minute = "00",
                                    mealRelation = MealRelation.AFTER_MEAL.name
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adaug\u0103",
                            tint = Indigo600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Adaug\u0103 or\u0103",
                            color = Indigo600,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            } // end scrollable content

            // === FIXED FOOTER ===
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
                        val scheduleData: List<ScheduleEntryData> = schedules.map { entry ->
                            ScheduleEntryData(
                                administrationTime = "${entry.hour}:${entry.minute}",
                                mealRelation = entry.mealRelation
                            )
                        }
                        onSave(
                            name,
                            doseValue,
                            selectedUnit.name,
                            scheduleData,
                            durationDays,
                            associatedDocumentId.ifEmpty { null }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo600,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
                ) {
                    Text(
                        text = "Salveaz\u0103",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        } // end outer Column

        // === TIME PICKER OVERLAY ===
        timePickerTargetIndex?.let { idx ->
            if (idx < schedules.size) {
                val target = schedules[idx]
                val timePickerState = rememberTimePickerState(
                    initialHour = target.hour.toInt(),
                    initialMinute = target.minute.toInt(),
                    is24Hour = true
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { timePickerTargetIndex = null }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppDimens.paddingDefault)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            ),
                        shape = RoundedCornerShape(AppDimens.cornerRadiusMedium),
                        color = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            MaterialTheme(
                                colorScheme = MaterialTheme.colorScheme.copy(primary = Indigo600)
                            ) {
                                TimePicker(state = timePickerState)
                            }
                            Spacer(modifier = Modifier.height(AppDimens.gapDefault))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { timePickerTargetIndex = null }) {
                                    Text(
                                        text = "Anuleaz\u0103",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        schedules[idx] = target.copy(
                                            hour = timePickerState.hour.toString().padStart(2, '0'),
                                            minute = timePickerState.minute.toString().padStart(2, '0')
                                        )
                                        timePickerTargetIndex = null
                                    }
                                ) {
                                    Text("OK")
                                }
                            }
                        }
                    }
                }
            }
        }
    } // end Box
    }
}

data class LocalScheduleEntry(
    val hour: String = "08",
    val minute: String = "00",
    val mealRelation: String = MealRelation.NO_MEAL_RELATION.name
)

private fun DoseUnit.toRoLabel(): String = when (this) {
    DoseUnit.TABLET -> "Comprimat"
    DoseUnit.CAPSULE -> "Capsul\u0103"
    DoseUnit.ML -> "ml"
    DoseUnit.DROPS -> "Pic\u0103turi"
}
